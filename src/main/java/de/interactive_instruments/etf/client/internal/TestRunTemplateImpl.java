/**
 * Copyright 2019-2020 interactive instruments GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package de.interactive_instruments.etf.client.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONObject;

import de.interactive_instruments.etf.client.*;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class TestRunTemplateImpl extends AbstractMetadata implements TestRunTemplate {

    private final TrtExecutionContext trtExecutionContext;
    private final TestObjectBaseType baseType;
    private final Collection<String> executableTestSuiteEids;

    public TestRunTemplateImpl(final TrtExecutionContext trtExecutionContext, final JSONObject jsonObject,
            final EtsCollection allExecutableTestSuites,
            final EtfCollection<TranslationTemplateBundle> translationTemplateBundleCollection) {
        super(jsonObject);
        this.trtExecutionContext = trtExecutionContext;

        final Object etsRefs = jsonObject.getJSONObject("executableTestSuites").get("executableTestSuite");
        if (etsRefs instanceof JSONObject) {
            executableTestSuiteEids = Collections.singleton(
                    eidFromUrl(((JSONObject) etsRefs).getString("href")));
        } else if (etsRefs instanceof JSONArray) {
            executableTestSuiteEids = new ArrayList<>();
            for (final Object tag : ((JSONArray) etsRefs)) {
                executableTestSuiteEids.add(
                        eidFromUrl(((JSONObject) tag).getString("href")));
            }
        } else {
            throw new ReferenceError(
                    "No Executable Test Suite found for Test Run Template " + eid());
        }
        final ExecutableTestSuite firstEts = allExecutableTestSuites.itemById(executableTestSuiteEids.iterator().next()).get();
        this.baseType = firstEts.supportedBaseType();

    }

    @Override
    public TestObjectBaseType supportedBaseType() {
        return this.baseType;
    }

    @Override
    public Collection<String> executableTestSuiteEids() {
        return Collections.unmodifiableCollection(executableTestSuiteEids);
    }

    @Override
    public TestRun execute(final TestObject testObject)
            throws RemoteInvocationException, IncompatibleTestObjectTypesException, IllegalStateException {
        return execute(testObject, null);
    }

    @Override
    public TestRun execute(final TestObject testObject, final TestRunObserver testRunObserver)
            throws RemoteInvocationException, IncompatibleTestObjectTypesException, IllegalStateException {

        if (!testObject.baseType().equals(this.baseType)) {
            throw new IncompatibleTestObjectTypesException();
        }
        return this.trtExecutionContext.start(this, testObject, testRunObserver);
    }
}