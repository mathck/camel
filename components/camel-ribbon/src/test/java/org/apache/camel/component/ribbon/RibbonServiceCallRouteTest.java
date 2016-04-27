/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.ribbon;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ServiceCallConfigurationDefinition;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class RibbonServiceCallRouteTest extends CamelTestSupport {

    @Test
    public void testServiceCall() throws Exception {
        getMockEndpoint("mock:9090").expectedMessageCount(1);
        getMockEndpoint("mock:9091").expectedMessageCount(1);
        getMockEndpoint("mock:result").expectedMessageCount(2);

        String out = template.requestBody("direct:start", null, String.class);
        String out2 = template.requestBody("direct:start", null, String.class);
        assertEquals("9091", out);
        assertEquals("9090", out2);

        assertMockEndpointsSatisfied();
    }

    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // setup a static ribbon server list
                List<RibbonServer> servers = new ArrayList<>();
                servers.add(new RibbonServer("localhost", 9090));
                servers.add(new RibbonServer("localhost", 9091));
                RibbonServiceCallServerListStrategy list = new RibbonServiceCallServerListStrategy(servers);



                // configure camel service call
                ServiceCallConfigurationDefinition config = new ServiceCallConfigurationDefinition();
                config.setServerListStrategy(list);

                from("direct:start")
                        .serviceCall("cdi-camel-jetty", null, config)
                        .to("mock:result");

                from("jetty:http://localhost:9090")
                    .to("mock:9090")
                    .transform().constant("9090");

                from("jetty:http://localhost:9091")
                    .to("mock:9091")
                    .transform().constant("9091");
            }
        };
    }
}

