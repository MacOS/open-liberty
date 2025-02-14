/*******************************************************************************
 * Copyright (c) 2011, 2024 IBM Corporation and others.
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

package com.ibm.ws.ejbcontainer.security.jacc_fat;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import com.ibm.websphere.simplicity.log.Log;

import componenttest.custom.junit.runner.FATRunner;
import componenttest.custom.junit.runner.Mode;
import componenttest.custom.junit.runner.Mode.TestMode;
import componenttest.rules.repeater.RepeatTests;

/**
 * This test variation covers packaging of EJB in standalone WAR file with EJB and servlet
 * classes in WEB-INF/classes. There is no application.xml or ibm-application-bnd.xml file.
 *
 * Performs testing of EJB pure annotations with a Stateless bean and application-bnd role mappings in server.xml.
 * This tests the class level annotations RunAs (Employee), RolesAllowed (Manager) and DeclareRoles(DeclaredRole01) with a
 * variety of method level annotations. This test invokes SecurityEJBA01Bean methods with a variety of method signatures to insure that
 * annotations are processed correctly with methods of the same name and different signature.
 *
 * This test covers the application-bnd role mappings in server.xml while PureAnnAppBndXMLBindingsTest covers
 * application-bnd mappings in ibm-application-bnd.xml.
 *
 * This test covers the positive RunAs testing while PureAnnA05Test covers the negative scenarios.
 */
@Mode(TestMode.FULL)
@RunWith(FATRunner.class)
public class PureAnnA01InWarTest extends PureAnnA01Base {

    protected static Class<?> logClass = PureAnnA01InWarTest.class;

    @Rule
    public TestName name = new TestName();

    @ClassRule
    public static RepeatTests r = FATSuite.defaultRepeat(Constants.SERVER_EJB);

    @BeforeClass
    public static void setUp() throws Exception {

        Log.info(logClass, "setUp", "Starting the server....");
        commonSetup(logClass, Constants.SERVER_EJB,
                    Constants.APPLICATION_SECURITY_EJB_STANDALONE_WAR, Constants.SERVLET_SECURITY_EJB, Constants.CONTEXT_ROOT_SECURITY_EJB_STANDALONE_WAR);

    }

    @Override
    protected TestName getName() {
        return name;
    }

}