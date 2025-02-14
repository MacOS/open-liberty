/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation and others.
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
package io.openliberty.org.hibernate.validator;

import test.common.OSSMessageTest;

/**
 * The purpose of this test is to ensure that we always pick up changes in Hibernate Validator NLS files so we can translate
 * the changes into languages that OpenLiberty ships but Hibernate Validator does not.
 * Currently OpenLiberty provides: cs, de, es, fr, hu, it, ja, ko, pl, pt_BR, ro, ru, zh_TW, zh
 * Currently Hibernate Validator provides: ar, cs, da, de, en, es, fa, fr, hu, it, ja, ko, mn_MN, nl, pl, pt_BR, ro, ru, sk, tr, uk, zh_CN, zh_TW, zh
 *
 * This means that OpenLiberty must provide the following languages: none for Hibernate Validator 9
 */
public class HibernateValidatorMessageTest extends OSSMessageTest {

    public HibernateValidatorMessageTest() {
        super("hibernate-validator-9", "io.openliberty.org.hibernate.validator.9.0", "org/hibernate/validator/ValidationMessages.properties");
    }

}
