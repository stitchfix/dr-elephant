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

package rest;

import com.fasterxml.jackson.databind.JsonNode;
import common.DBTestUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Application;
import play.GlobalSettings;
///import play.libs.WS;
import play.libs.ws.*;
import play.libs.ws.WS;
import play.test.FakeApplication;

import static common.DBTestUtil.*;
import static common.TestConstants.*;
import static org.junit.Assert.assertTrue;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;


/**
 * <p>
 * Class aims to exercise all the rest end points exposed by Dr.Elephant
 * </p>
 * <p>
 * A fake application connecting to an in-memory H2 DB is started inside<br>
 * the test server which runs the test code. The global class is overridden<br>
 * so that we don't have to go through the regular application start flow.
 * </p>
 */
public class RestAPITest {

  private static final Logger logger = LoggerFactory.getLogger(RestAPITest.class);
  private static Application fakeApp;

  @Before
  public void setup() {
    Map<String, String> dbConn = new HashMap<String, String>();
    dbConn.put(DB_DEFAULT_DRIVER_KEY, DB_DEFAULT_DRIVER_VALUE);
    dbConn.put(DB_DEFAULT_URL_KEY, DB_DEFAULT_URL_VALUE);
    dbConn.put(EVOLUTION_PLUGIN_KEY, EVOLUTION_PLUGIN_VALUE);
    dbConn.put(APPLY_EVOLUTIONS_DEFAULT_KEY, APPLY_EVOLUTIONS_DEFAULT_VALUE);

    GlobalSettings gs = new GlobalSettings() {
      @Override
      public void onStart(Application app) {
        logger.info("Starting FakeApplication");
      }
    };

    fakeApp = fakeApplication(dbConn, gs);
  }

  /**
   * <p>
   * Rest API - Performs search by job ID
   * <br>
   * API provides information on the specific job
   * </p>
   * <p>
   * Following assertions are made in the response json
   *   <ul>Job id</ul>
   *   <ul>Job name</ul>
   *   <ul>Job type</ul>
   * </p>
   */
  @Test
  public void testrestAppResult() {
    running(testServer(TEST_SERVER_PORT, fakeApp), new Runnable() {
      public void run() {
    	  try {
        populateTestData();
        final WSResponse response = WS.url(BASE_URL + REST_APP_RESULT_PATH).
            setQueryParameter("id", TEST_JOB_ID1).
            get().toCompletableFuture().get(RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS);
        final JsonNode jsonResponse = response.asJson();
        assertTrue("Job id did not match", TEST_JOB_ID1.equals(jsonResponse.path("id").asText()));
        assertTrue("Job name did not match", TEST_JOB_NAME.equals(jsonResponse.path("name").asText()));
        assertTrue("Job type did not match", TEST_JOB_TYPE.equals(jsonResponse.path("jobType").asText()));
       } catch(Exception exc) {
    	   throw new RuntimeException(exc);
       }
      }
    });
  }

  /**
   * <p>
   * Rest API - Performs search by job execution ID
   * <br>
   * API returns all jobs triggered by a particular Scheduler Job
   * </p>
   * <p>
   * Following assertions are made in the response json
   *   <ul>Job id</ul>
   *   <ul>Job execution id</ul>
   * </p>
   */
  @Test
  public void testrestJobExecResult() {
    running(testServer(TEST_SERVER_PORT, fakeApp), new Runnable() {
      public void run() {
    	  try {
        populateTestData();
        final WSResponse response = WS.url(BASE_URL + REST_JOB_EXEC_RESULT_PATH).
            setQueryParameter("id", TEST_JOB_EXEC_ID1).
            get().toCompletableFuture().get(RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS);
        System.out.println(" RESPONSE " + response.getBody()  );
        final JsonNode jsonResponse = response.asJson().get(0);
        assertTrue("Job id did not match", TEST_JOB_ID1.equals(jsonResponse.path("id").asText()));
        assertTrue("Job execution id did not match", TEST_JOB_EXEC_ID1.equals(jsonResponse.path("jobExecId").asText()));
       } catch(Exception exc) {
    	   throw new RuntimeException(exc);
       }
      }
    });
  }

  /**
   * <p>
   * Rest API - Performs search by flow execution ID
   * <br>
   * API returns all jobs under a particular flow execution
   * </p>
   * <p>
   * Following assertions are made in the response json
   *   <ul>Job id</ul>
   *   <ul>Flow execution id</ul>
   * </p>
   */
  @Test
  public void testrestFlowExecResult()  {
    running(testServer(TEST_SERVER_PORT, fakeApp), new Runnable() {
      public void run()  {
       try {
        populateTestData();
        final WSResponse response = WS.url(BASE_URL + REST_FLOW_EXEC_RESULT_PATH).
            setQueryParameter("id", TEST_FLOW_EXEC_ID1).
            get().toCompletableFuture().get(RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS);
        final JsonNode jsonResponse = response.asJson();
        assertTrue("Job id did not match", TEST_JOB_ID1.equals(jsonResponse.findValue("id").asText()));
        assertTrue("Flow execution id did not match",
            TEST_FLOW_EXEC_ID1.equals(jsonResponse.findValue("flowExecId").asText()));
       } catch(Exception exc) {
    	   throw new RuntimeException(exc);
       }
      }
    });
  }

  /**
   * <p>
   * Rest API - Perform a generic search or search by filter criteria
   * <br>
   * Test verifies if all available flows are returned
   * </p>
   * <p>
   * Following assertions are made in the response json
   *   <ul>First job id</ul>
   *   <ul>Second job id</ul>
   * </p>
   */
  @Test
  public void testrestSearch() {
    running(testServer(TEST_SERVER_PORT, fakeApp), new Runnable() {
      public void run() {
  	    try {
          populateTestData();
           final WSResponse response = WS.url(BASE_URL + REST_SEARCH_PATH).
               get().toCompletableFuture().get(RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS);
           List<String> jobList = response.asJson().findValuesAsText("id");
           assertTrue("Job id1 missing in list", jobList.contains(TEST_JOB_ID1));
           assertTrue("Job id2 missing in list", jobList.contains(TEST_JOB_ID2));
         } catch(Exception exc) {
    	   throw new RuntimeException(exc);
         }
      }
    });
  }

  /**
   * <p>
   * Rest API - Perform a search with additional params
   * <br>
   * Test verifies if specific flow is returned
   * </p>
   * <p>
   * Following assertions are made in the response json
   *   <ul>No of jobs returned</ul>
   *   <ul>Job id</ul>
   *   <ul>Username</ul>
   *   <ul>Job type</ul>
   * </p>
   */
  @Test
  public void testrestSearchWithUsernameAndJobType() {
    running(testServer(TEST_SERVER_PORT, fakeApp), new Runnable() {
      public void run() {
       try {
        populateTestData();
        final WSResponse response = WS.url(BASE_URL + REST_SEARCH_PATH).
            setQueryParameter("username", TEST_USERNAME).
            setQueryParameter("", TEST_JOB_TYPE).
            get().toCompletableFuture().get(RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS);
        JsonNode reponseJson = response.asJson();
        List<String> jobList = reponseJson.findValuesAsText("id");
        assertTrue("More than one row returned", jobList.size() == 1);
        assertTrue("Job id missing in response", TEST_JOB_ID1.equals(reponseJson.findValue("id").asText()));
        assertTrue("Username incorrect", TEST_USERNAME.equals(reponseJson.findValue("username").asText()));
        assertTrue("Job type incorrect", TEST_JOB_TYPE.equals(reponseJson.findValue("jobType").asText()));
       } catch(Exception exc) {
    	   throw new RuntimeException(exc);
       }
      }
    });
  }

  /**
   * <p>
   * Rest API - Compares two flow executions by flow execution ID
   * </p>
   * <p>
   * Following assertions are made in the response json
   *   <ul>Second job ID</ul>
   * </p>
   */
  @Test
  public void testrestCompare() {
    running(testServer(TEST_SERVER_PORT, fakeApp), new Runnable() {
      public void run() {
       try {
        populateTestData();
        final WSResponse response = WS.url(BASE_URL + REST_COMPARE_PATH).
            setQueryParameter("flow-exec-id1", TEST_FLOW_EXEC_ID1).
            setQueryParameter("flow-exec-id2", TEST_FLOW_EXEC_ID2).
            get().toCompletableFuture().get(RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS);
        assertTrue("Job id did not match", TEST_JOB_ID2.equals(response.asJson().findValue("id").asText()));
       } catch(Exception exc) {
    	   throw new RuntimeException(exc);
       }
      }
    });
  }

  /**
   * <p>
   * Rest API - Provides data for plotting the flow history graph
   * </p>
   * <p>
   * Following assertions are made in the response json
   *   <ul>First job execution ID</ul>
   *   <ul>Second job execution ID</ul>
   * </p>
   */
  @Test
  public void testrestFlowGraphData() {
    running(testServer(TEST_SERVER_PORT, fakeApp), new Runnable() {
      public void run() {
       try {
        populateTestData();
        final WSResponse response = WS.url(BASE_URL + REST_FLOW_GRAPH_DATA_PATH).
            setQueryParameter("id", TEST_FLOW_DEF_ID1).
            get().toCompletableFuture().get(RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS);
        List<String> jobList = response.asJson().findValuesAsText("jobexecurl");
        assertTrue("Job exec url1 missing in list", jobList.contains(TEST_JOB_EXEC_ID1));
        assertTrue("Job exec url2 missing in list", jobList.contains(TEST_JOB_EXEC_ID2));
       } catch(Exception exc) {
    	   throw new RuntimeException(exc);
       }
      }
    });
  }

  /**
   * <p>
   * Rest API - Provides data for plotting the job history graph
   * </p>
   * <p>
   * Following assertions are made in the response json
   *   <ul>First job id</ul>
   *   <ul>Second job id</ul>
   * </p>
   */
  @Test
  public void testrestJobGraphData() {
    running(testServer(TEST_SERVER_PORT, fakeApp), new Runnable() {
      public void run() {
       try {
        populateTestData();
        final WSResponse response = WS.url(BASE_URL + REST_JOB_GRAPH_DATA_PATH).
            setQueryParameter("id", TEST_JOB_DEF_ID1).
            get().toCompletableFuture().get(RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS);
        List<String> jobList = response.asJson().findValuesAsText("stageid");
        assertTrue("Job id 1 missing in list", jobList.contains(TEST_JOB_ID1));
        assertTrue("Job id 2 missing in list", jobList.contains(TEST_JOB_ID2));
       } catch(Exception exc) {
    	   throw new RuntimeException(exc);
       }
      }
    });
  }

  private void populateTestData() {
    try {
      initDB();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
