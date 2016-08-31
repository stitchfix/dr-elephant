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

package com.linkedin.drelephant;

import org.junit.Test;

import com.linkedin.drelephant.analysis.ApplicationType;
import com.linkedin.drelephant.analysis.ElephantFetcher;

import play.Application;
import play.Play;
import play.test.FakeApplication;
import java.io.*;


public class ElephantContextTest {


  /**
   *  Test instantiating an ElephantContext
   */
	/// UGGGGHHH !!!! Java Play code
  ///@Test
  public void testCreateContext() {
	  ElephantContext ctxt =  ElephantContext.instance();
  }

  ///@Test
  public void testGetSparkFetcher() {
	  File conf = new File("./test/resources/application.conf");
	  Application fakeApp = new FakeApplication(conf,this.getClass().getClassLoader(), new java.util.HashMap()  );
	  ////Play.start(fakeApp);

	  ElephantContext ctxt =  ElephantContext.instance();
	  ApplicationType sparkType = ctxt.getApplicationTypeForName("SPARK");
      ElephantFetcher fetcher =  ctxt.getFetcherForApplicationType(sparkType);
      ///Play.stop( fakeApp);
  }

}

