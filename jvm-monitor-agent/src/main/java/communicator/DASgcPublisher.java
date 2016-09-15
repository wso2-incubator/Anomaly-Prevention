/*
*  Copyright (c) ${date}, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package communicator;

import jvmmonitor.model.GarbageCollectionLog;
import org.apache.log4j.Logger;
import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.*;
import java.util.Enumeration;
import java.util.LinkedList;


public class DASgcPublisher {

    private DataPublisher dataPublisher;
    private EventPublisher eventAgent;
    private String dataStream;
    private String gcLogStream;
    private String appID = "";

    final static Logger logger = Logger.getLogger(DASgcPublisher.class);

    /**
     * Need to set client-truststore.jks file located path
     *
     * @param defaultThriftPort
     * @param defaultBinaryPort
     * @param username
     * @param password
     * @throws SocketException
     * @throws UnknownHostException
     * @throws DataEndpointAuthenticationException
     * @throws DataEndpointAgentConfigurationException
     * @throws TransportException
     * @throws DataEndpointException
     * @throws DataEndpointConfigurationException
     */
    public DASgcPublisher(int defaultThriftPort, int defaultBinaryPort, String username, String password) throws SocketException,
            UnknownHostException,
            DataEndpointAuthenticationException,
            DataEndpointAgentConfigurationException,
            TransportException,
            DataEndpointException,
            DataEndpointConfigurationException {

        logger.info("Starting DAS HttpLog Agent");
        String currentDir = System.getProperty("user.dir");
        System.setProperty("javax.net.ssl.trustStore", currentDir + "/jvm-monitor-agent/src/main/resources/client-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");

        AgentHolder.setConfigPath(getDataAgentConfigPath());
        String host = getLocalAddress().getHostAddress();

        String type = getProperty("type", "Thrift");
        int receiverPort = defaultThriftPort;
        if (type.equals("Binary")) {
            receiverPort = defaultBinaryPort;
        }
        int securePort = receiverPort + 100;

        String url = getProperty("url", "tcp://" + host + ":" + receiverPort);
        String authURL = getProperty("authURL", "ssl://" + host + ":" + securePort);
        username = getProperty("username", username);
        password = getProperty("password", password);

        dataPublisher = new DataPublisher(type, url, authURL, username, password);

        //Set default Garbage collection log Stream
        String HTTPD_LOG_STREAM = "GarbageCollectionStream";
        String VERSION = "1.0.0";
        setDataStream(HTTPD_LOG_STREAM, VERSION);

        eventAgent = new EventPublisher();

    }

    /**
     * Need to set this to identify particular application
     *
     * @param appID
     */
    public void setAppID(String appID) {
        this.appID = appID;
    }

    /**
     * Shutdown the DataPublisher
     *
     * @throws DataEndpointException
     */
    public void shutdownDataPublisher() throws DataEndpointException {
        dataPublisher.shutdown();
    }

    /**
     * Generate StreamId for Garbage Collection data
     * <p>
     * Data format must be in the following order in given types in "GarbageCollectionStream":-
     * <p>
     * String	GC_TYPE
     * long     GC_DURATION
     * long     GC_START_TIME
     * String	GC_CAUSE
     * long     EDEN_USED_MEMORY_AFTER_GC
     * long     EDEN_USED_MEMORY_BEFORE_GC
     * long	    SURVIVOR_USED_MEMORY_AFTER_GC
     * long	    SURVIVOR_USED_MEMORY_BEFORE_GC
     * long	    OLD_GEN_USED_MEMORY_AFTER_GC
     * long  	OLD_GEN_USED_MEMORY_BEFORE_GC
     * long 	EDEN_COMMITTED_MEMORY_AFTER_GC
     * long 	EDEN_COMMITTED_MEMORY_BEFORE_GC
     * long 	SURVIVOR_COMMITTED_MEMORY_AFTER_GC
     * long 	SURVIVOR_COMMITTED_MEMORY_BEFORE_GC
     * long 	OLD_GEN_COMMITTED_MEMORY_AFTER_GC
     * long 	OLD_GEN_COMMITTED_MEMORY_BEFORE_GC
     * long 	EDEN_MAX_MEMORY_AFTER_GC
     * long 	EDEN_MAX_MEMORY_BEFORE_GC
     * long 	SURVIVOR_MAX_MEMORY_AFTER_GC
     * long 	SURVIVOR_MAX_MEMORY_BEFORE_GC
     * long 	OLD_GEN_MAX_MEMORY_AFTER_GC
     * long 	OLD_GEN_MAX_MEMORY_BEFORE_GC
     *
     * @param HTTPD_LOG_STREAM
     * @param VERSION
     */
    public void setDataStream(String HTTPD_LOG_STREAM, String VERSION) {
        dataStream = DataBridgeCommonsUtils.generateStreamId(HTTPD_LOG_STREAM, VERSION);
    }

    /**
     * Generate StreamId for Garbage Collection data
     * Garbage Collection data should be in format -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps
     * <p>
     * Data format must be in the following order in given types in "gcLogStream":-
     * <p>
     * String   fileID
     * String	Date
     * String   TimeStarted
     * double   TimePass
     * String   GCFlage
     * String   CaseCollection
     * String   GCName
     * long     YoungGenerationBefore
     * long     YoungGenerationAfter
     * long     TotalYoungGeneration
     * long     OldGenerationBefore
     * long     OldGenerationAfter
     * long     TotalOldGeneration
     * long     MetaspaceGenerationBefore
     * long     MetaspaceGenerationAfter
     * long     TotalMetaspaceGeneration
     * long     TotalUsedHeapBefore
     * long     TotalUsedHeapAfter
     * long     TotalAvailableHeap
     * double   GCEventDuration
     * double   GCEventUserTimes
     * double   GCEventSysTimes
     * double   GCEventRealTimes
     *
     * @param HTTPD_LOG_STREAM
     * @param VERSION
     */
    private void setGcLogStream(String HTTPD_LOG_STREAM, String VERSION) {
        gcLogStream = DataBridgeCommonsUtils.generateStreamId(HTTPD_LOG_STREAM, VERSION);
    }

    /**
     * @param garbageCollectionLog
     * @throws DataEndpointAuthenticationException
     * @throws DataEndpointAgentConfigurationException
     * @throws DataEndpointException
     * @throws DataEndpointConfigurationException
     * @throws TransportException
     */
    public void publishGCData(LinkedList<GarbageCollectionLog> garbageCollectionLog) throws DataEndpointAuthenticationException,
            DataEndpointAgentConfigurationException,
            DataEndpointException,
            DataEndpointConfigurationException,
            TransportException {

        //HTTPD_LOG_STREAM = "GarbageCollectionStream"
        //VERSION = "1.0.0"

        while (!garbageCollectionLog.isEmpty()) {
            eventAgent.publishLogEvents(dataPublisher, dataStream, appID, garbageCollectionLog.poll());
        }

    }

    /**
     * @param fileName
     * @throws TransportException
     * @throws DataEndpointConfigurationException
     * @throws FileNotFoundException
     * @throws DataEndpointAuthenticationException
     * @throws DataEndpointException
     * @throws DataEndpointAgentConfigurationException
     */
    public void publishXXgcLogData(String fileName) throws TransportException,
            DataEndpointConfigurationException,
            FileNotFoundException,
            DataEndpointAuthenticationException,
            DataEndpointException,
            DataEndpointAgentConfigurationException {

        //HTTPD_LOG_STREAM = "gcLogStream"
        //VERSION = "1.0.0"

        eventAgent.publishLogEvents(dataPublisher, gcLogStream, fileName);

    }

    /**
     * Need to set resource files located path
     *
     * @return
     */
    public static String getDataAgentConfigPath() {
        File filePath = new File("jvm-monitor-agent" + File.separator + "src" + File.separator + "main" + File.separator + "resources");
        if (!filePath.exists()) {
            filePath = new File("test" + File.separator + "resources");
        }
        if (!filePath.exists()) {
            filePath = new File("resources");
        }
        return filePath.getAbsolutePath() + File.separator + "data-agent-conf.xml";
    }

    public static InetAddress getLocalAddress() throws SocketException, UnknownHostException {
        Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
        while (ifaces.hasMoreElements()) {
            NetworkInterface iface = ifaces.nextElement();
            Enumeration<InetAddress> addresses = iface.getInetAddresses();

            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                    return addr;
                }
            }
        }
        return InetAddress.getLocalHost();
    }

    private static String getProperty(String name, String def) {
        String result = System.getProperty(name);
        if (result == null || result.length() == 0 || result == "") {
            result = def;
        }
        return result;
    }


}
