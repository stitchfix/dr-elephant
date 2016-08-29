/*
 * Copyright 2016 LinkedIn Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.linkedin.drelephant.util;


import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 * This class tests the Utils class
 */
public class UtilsTest {

  @Test
  public void testParseJavaOptions() {
    Map<String, String> options1 = Utils.parseJavaOptions("-Dfoo=bar");
    assertEquals(1, options1.size());
    assertEquals("bar", options1.get("foo"));

    Map<String, String> options2 = Utils.parseJavaOptions(" -Dfoo=bar   -Dfoo2=bar2 -Dfoo3=bar3");
    assertEquals(3, options2.size());
    assertEquals("bar", options2.get("foo"));
    assertEquals("bar2", options2.get("foo2"));
    assertEquals("bar3", options2.get("foo3"));
  }

  @Test
  public void testParseJavaOptionsWithSystemProps() {
   String sysProps = "-Dlog4j.configuration=file:///etc/spark/conf/log4j.properties -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=70 -XX:MaxHeapFreeRatio=70 -XX:-CMSClassUnloadingEnabled -XX:MaxPermSize=512M -XX:OnOutOfMemoryError='kill -9 %p'";

    Map<String, String> options1 = Utils.parseJavaOptions(sysProps);
    assertEquals(7, options1.size());
    assertEquals("file:///etc/spark/conf/log4j.properties", options1.get("log4j.configuration"));
    assertEquals("true", options1.get("UseConcMarkSweepGC"));
    assertEquals("false", options1.get("CMSClassUnloadingEnabled"));
    assertEquals("70", options1.get("CMSInitiatingOccupancyFraction"));
    assertEquals("512M", options1.get("MaxPermSize"));
    assertEquals("kill -9 %p", options1.get("OnOutOfMemoryError"));

  }
  @Test
  public void testJavaParamParser() {
	 String[] props = ParamParser.parseJavaParam("-Dlog4j.configuration=file:///etc/spark/conf/log4j.properties");
	 System.out.println(" PROP is " + props[0] + " == " + props[1]);
	 
	 assertEquals(props[0], "log4j.configuration");
	 assertEquals(props[1], "file:///etc/spark/conf/log4j.properties");
  }

  @Test
  public void testSysParamParser() {
	 String[] props = ParamParser.parseJavaParam("-XX:MaxHeapFreeRatio=70");
	 System.out.println(" PROP is " + props[0] + " == " + props[1]);
	 
	 assertEquals(props[0], "MaxHeapFreeRatio");
	 assertEquals(props[1], "70");
  }

  @Test
  public void testSysFlagParser() {
	 String[] props = ParamParser.parseJavaParam("-XX:+UseConcMarkSweepGC");
	 System.out.println(" PROP is " + props[0] + " == " + props[1]);
	 
	 assertEquals(props[0], "UseConcMarkSweepGC");
	 assertEquals(props[1], "true");
  }


  @Test
  public void testQuotedParser() {
	 String[] props = ParamParser.parseJavaParam("-XX:OnOutOfMemoryError='kill -9 %p'");
	 System.out.println(" PROP is " + props[0] + " == " + props[1]);
	 
	 assertEquals(props[0], "OnOutOfMemoryError");
	 assertEquals(props[1], "kill -9 %p");
  }




  @Test
  public void testGetParam() {
    Map<String, String> paramMap = new HashMap<String, String>();
    paramMap.put("test_severity_1", "10, 50, 100, 200");
    paramMap.put("test_severity_2", "2, 4, 8");
    paramMap.put("test_param_1", "2&");
    paramMap.put("test_param_2", "2");
    paramMap.put("test_param_3", "");
    paramMap.put("test_param_4", null);

    double limits1[] = Utils.getParam(paramMap.get("test_severity_1"), 4);
    assertEquals(10d, limits1[0], 0);
    assertEquals(50d, limits1[1], 0);
    assertEquals(100d, limits1[2], 0);
    assertEquals(200d, limits1[3], 0);

    double limits2[] = Utils.getParam(paramMap.get("test_severity_2"), 4);
    assertEquals(null, limits2);

    double limits3[] = Utils.getParam(paramMap.get("test_param_1"), 1);
    assertEquals(null, limits3);

    double limits4[] = Utils.getParam(paramMap.get("test_param_2"), 1);
    assertEquals(2d, limits4[0], 0);

    double limits5[] = Utils.getParam(paramMap.get("test_param_3"), 1);
    assertEquals(null, limits5);

    double limits6[] = Utils.getParam(paramMap.get("test_param_4"), 1);
    assertEquals(null, limits6);
  }

  @Test
  public void testCommaSeparated() {
    String commaSeparated1 = Utils.commaSeparated("foo");
    assertEquals("foo", commaSeparated1);

    String commaSeparated2 = Utils.commaSeparated("foo", "bar", "");
    assertEquals("foo,bar", commaSeparated2);

    String commaSeparated3 = Utils.commaSeparated("foo", "bar", null);
    assertEquals("foo,bar", commaSeparated3);

    String commaSeparated4 = Utils.commaSeparated();
    assertEquals("", commaSeparated4);
  }

  @Test
  public void testTruncateField() {
    String truncatedField1 = Utils.truncateField("foo-bar", 7, "id");
    assertEquals("foo-bar", truncatedField1);

    String truncatedField2 = Utils.truncateField("foo-bar", 6, "id");
    assertEquals("foo...", truncatedField2);

    String truncatedField3 = Utils.truncateField("foo-bar", -1, "id");
    assertEquals("foo-bar", truncatedField3);

    String truncatedField4 = Utils.truncateField(null, 5, "id");
    assertEquals(null, truncatedField4);
  }

  @Test
  public void testParseCsKeyValue() {
    Map<String, String> properties = Utils.parseCsKeyValue("");
    assertEquals(0, properties.size());

    Map<String, String> properties1 = Utils.parseCsKeyValue("foo=bar");
    assertEquals(1, properties1.size());
    assertEquals("bar", properties1.get("foo"));

    Map<String, String> properties2 = Utils.parseCsKeyValue("foo1=bar1,foo2=bar2,foo3=bar3");
    assertEquals(3, properties2.size());
    assertEquals("bar1", properties2.get("foo1"));
    assertEquals("bar2", properties2.get("foo2"));
    assertEquals("bar3", properties2.get("foo3"));
  }

  @Test
  public void testGetNonNegativeInt() {
    Configuration conf = new Configuration();
    conf.set("foo1", "100");
    conf.set("foo2", "-100");
    conf.set("foo3", "0");
    conf.set("foo4", "0.5");
    conf.set("foo5", "9999999999999999");
    conf.set("foo6", "bar");

    int defaultValue = 50;
    assertEquals(100, Utils.getNonNegativeInt(conf, "foo1", defaultValue));
    assertEquals(0, Utils.getNonNegativeInt(conf, "foo2", defaultValue));
    assertEquals(0, Utils.getNonNegativeInt(conf, "foo3", defaultValue));
    assertEquals(defaultValue, Utils.getNonNegativeInt(conf, "foo4", defaultValue));
    assertEquals(defaultValue, Utils.getNonNegativeInt(conf, "foo5", defaultValue));
    assertEquals(defaultValue, Utils.getNonNegativeInt(conf, "foo6", defaultValue));
    assertEquals(defaultValue, Utils.getNonNegativeInt(conf, "foo7", defaultValue));
  }

  @Test
  public void testGetNonNegativeLong() {
    Configuration conf = new Configuration();

    conf.set("foo1", "100");
    conf.set("foo2", "-100");
    conf.set("foo3", "0");
    conf.set("foo4", "0.5");
    conf.set("foo5", "9999999999999999");
    conf.set("foo6", "bar");

    long defaultValue = 50;
    assertEquals(100, Utils.getNonNegativeLong(conf, "foo1", defaultValue));
    assertEquals(0, Utils.getNonNegativeLong(conf, "foo2", defaultValue));
    assertEquals(0, Utils.getNonNegativeLong(conf, "foo3", defaultValue));
    assertEquals(defaultValue, Utils.getNonNegativeLong(conf, "foo4", defaultValue));
    assertEquals(9999999999999999L, Utils.getNonNegativeLong(conf, "foo5", defaultValue));
    assertEquals(defaultValue, Utils.getNonNegativeLong(conf, "foo6", defaultValue));
    assertEquals(defaultValue, Utils.getNonNegativeLong(conf, "foo7", defaultValue));
  }

  @Test
  public void testFormatStringOrNull() {
    assertEquals("Hello world!", Utils.formatStringOrNull("%s %s!", "Hello", "world"));
    assertEquals(null, Utils.formatStringOrNull("%s %s!", "Hello", null));
  }
}
