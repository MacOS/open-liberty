/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.grpc.fat.helloworld.service;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloReply;
import io.grpc.examples.helloworld.HelloRequest;
import io.grpc.stub.StreamObserver;
import io.openliberty.grpc.annotation.GrpcService;

/**
 * A simple gRPC service that can be deployed on Liberty with the grpcServlet-1.0 feature.
 *
 * This implementation specifies a "user" role. Any calls to sayHello that are not authenticated
 * with a user in the "user" role will fail with an UNAUTHENTICATED status.
 */
@WebServlet(urlPatterns = { "/helloWorld" }, asyncSupported = true)
public class HelloWorldServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * GreeterGrpc service implementation.
     *
     * Optional @GrpcService annotation registers HelloWorldServerAnnotationInterceptor and
     * HelloWorldServerAnnotationInterceptor2
     */
    @GrpcService(interceptors = { HelloWorldServerAnnotationInterceptor.class,
                                  HelloWorldServerAnnotationInterceptor2.class })
    public static final class GreeterImpl extends GreeterGrpc.GreeterImplBase {

        @Inject
        GreetingCDIBean greetingBean;

        // a public no-arg constructor is requird for Liberty to start this service automatically
        public GreeterImpl() {

        }

        @Override
        public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {

            String greetingMessage = "Hello";

            // GreetingCDIBean will only be available if CDI is enabled
            if (greetingBean != null) {
                greetingMessage = greetingBean.getGreeting();
            }

            // this context will only be available if CDI is available and HelloWorldServerCDIInterceptor is active
            Object exampleContextObject = HelloWorldServerCDIInterceptor.EXAMPLE_CONTEXT_KEY.get();
            if (exampleContextObject != null) {
                greetingMessage = (String) exampleContextObject + "; " + greetingMessage;
            }

            HelloReply reply = HelloReply.newBuilder().setMessage(greetingMessage + " " + req.getName()).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        response.getWriter().println("Hello from HelloWorldServlet!");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        response.getWriter().println("Hello from HelloWorldServlet!");
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}