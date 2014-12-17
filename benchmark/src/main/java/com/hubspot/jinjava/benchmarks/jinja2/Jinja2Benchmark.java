package com.hubspot.jinjava.benchmarks.jinja2;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.loader.FileLocator;


@State(Scope.Benchmark)
public class Jinja2Benchmark {
  
  public String simpleTemplate;
  public Map<String, ?> simpleBindings;
  
  public String complexTemplate;
  public Map<String, ?> complexBindings;
  
  public Jinjava jinjava;
  
  @SuppressWarnings("unchecked")
  @Setup
  public void setup() throws IOException, NoSuchAlgorithmException {
    ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
    logger.setLevel(Level.WARN);
    
    jinjava = new Jinjava();
    jinjava.setResourceLocator(new FileLocator(new File("jinja2/examples/rwbench/jinja")));
    
    simpleTemplate = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("jinja/simple.jinja"));
    
    List<Map<String, Integer>> table = new ArrayList<>();
    for(int i = 0; i < 1000; i++) {
      Map<String, Integer> row = new LinkedHashMap<>();
      for(int j = 0; j < 10; j++) {
        row.put(String.valueOf('a' + j), j + 1);
      }
      table.add(row);
    }
    
    simpleBindings = ImmutableMap.of("page_title", "my page title", "table", table);
    
    complexTemplate = FileUtils.readFileToString(new File("jinja2/examples/rwbench/jinja/index.html"), Charsets.UTF_8);
    // for tag doesn't support postfix conditional filtering
    complexTemplate = complexTemplate.replaceAll(" if article.published", "");
    
    List<User> users = Lists.newArrayList(new User("John Doe"), new User("Jane Doe"), new User("Peter Somewhat"));
    SecureRandom rnd = SecureRandom.getInstanceStrong();
    List<Article> articles = new ArrayList<>();
    for(int i = 0; i < 20; i++) {
      articles.add(new Article(i, users.get(rnd.nextInt(users.size()))));
    }
    List<ArrayList<String>> navigation = Lists.newArrayList(
        Lists.newArrayList("index", "Index"), 
        Lists.newArrayList("about", "About"),
        Lists.newArrayList("foo?bar=1", "Foo with Bar"),
        Lists.newArrayList("foo?bar=2&s=x", "Foo with X"),
        Lists.newArrayList("blah", "Blub Blah"),
        Lists.newArrayList("hehe", "Haha")
    );
    
    complexBindings = ImmutableMap.of("users", users, "articles", articles, "navigation", navigation);
  }

  @Benchmark
  public String semiRealWorldBenchmark() {
    return jinjava.render(simpleTemplate, simpleBindings);
  }
  
  @Benchmark
  public String moreRealWorldBenchmark() {
    return jinjava.render(complexTemplate, complexBindings);
  }

  public static void main(String[] args) throws Exception {
    Jinja2Benchmark b = new Jinja2Benchmark();
    b.setup();
    System.out.println(b.semiRealWorldBenchmark());
    System.out.println(b.moreRealWorldBenchmark());
  }
  
}