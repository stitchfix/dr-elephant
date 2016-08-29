//
// Copyright 2016 LinkedIn Corp.
//
// Licensed under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License. You may obtain a copy of
// the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
// License for the specific language governing permissions and limitations under
// the License.
//

import play.sbt.PlayImport._
import sbt._

object Dependencies {

  // Dependency Version
  val commonsCodecVersion = "1.10"
  val commonsIoVersion = "2.4"
  val gsonVersion = "2.2.4"
  val guavaVersion = "18.0"          // Hadoop defaultly are using guava 11.0, might raise NoSuchMethodException
  val jacksonMapperAslVersion = "1.7.3"
  val jsoupVersion = "1.7.3"
  val mysqlConnectorVersion = "5.1.36"

  val HADOOP_VERSION = "hadoopversion"
  val SPARK_VERSION = "sparkversion"

  val sparkScalaVersion = "2.11" /// Need to be compatible with Play

  def getProperty( prop: String, default:String ) : String = {
    if (System.getProperties.getProperty(prop) != null) {
      System.getProperties.getProperty(prop)
    } else {
      default
    }
  }

  val hadoopVersion = getProperty(HADOOP_VERSION, "2.6.0")

  ///val sparkVersion = getProperty(SPARK_VERSION, "1.5.2") 
  val sparkVersion = getProperty(SPARK_VERSION, "1.6.1") 

  val sparkExclusion = if (sparkVersion >= "1.5.0") {
    "org.apache.spark" % s"spark-core_${sparkScalaVersion}" % sparkVersion excludeAll(
      ExclusionRule(organization = "com.typesafe.akka"),
      ExclusionRule(organization = "org.apache.avro"),
      ExclusionRule(organization = "org.apache.hadoop"),
      ExclusionRule(organization = "net.razorvine")
    )
  } else {
    "org.apache.spark" % "spark-core_${sparkScalaVersion}" % sparkVersion excludeAll(
      ExclusionRule(organization = "org.apache.avro"),
      ExclusionRule(organization = "org.apache.hadoop"),
      ExclusionRule(organization = "net.razorvine")
    )
  }

  // Log4j dependencies ... instead of logback
  val log4jDeps = Seq(
    "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.4.1",
    "org.apache.logging.log4j" % "log4j-api" % "2.4.1",
    "org.apache.logging.log4j" % "log4j-core" % "2.4.1"
  )


  // Dependency coordinates
  val requiredDep = Seq(
    "com.google.code.gson" % "gson" % gsonVersion,
    "com.google.guava" % "guava" % guavaVersion,
    "commons-codec" % "commons-codec" % commonsCodecVersion,
    "commons-io" % "commons-io" % commonsIoVersion,
    "mysql" % "mysql-connector-java" % mysqlConnectorVersion,
    "org.apache.hadoop" % "hadoop-auth" % hadoopVersion % "compileonly",
    "org.apache.hadoop" % "hadoop-common" % hadoopVersion % "compileonly",
    "org.apache.hadoop" % "hadoop-common" % hadoopVersion % Test,
    "org.apache.hadoop" % "hadoop-hdfs" % hadoopVersion % "compileonly",
    "org.apache.hadoop" % "hadoop-hdfs" % hadoopVersion % Test,
    "org.codehaus.jackson" % "jackson-mapper-asl" % jacksonMapperAslVersion,
    "org.jsoup" % "jsoup" % jsoupVersion,
    "com.lihaoyi" %% "fastparse" % "0.3.7",
    "org.mockito" % "mockito-core" % "1.10.19",
    "org.jmockit" % "jmockit" % "1.23" % Test
  ) :+ sparkExclusion 

  var dependencies = Seq(javaJdbc, cache, javaWs)
  dependencies ++= requiredDep ++ log4jDeps
}
