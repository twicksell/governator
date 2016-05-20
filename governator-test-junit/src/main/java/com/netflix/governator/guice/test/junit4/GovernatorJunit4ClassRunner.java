package com.netflix.governator.guice.test.junit4;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.internal.runners.statements.RunAfters;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import com.netflix.governator.guice.test.AnnotationBasedTestInjectorCreator;
import com.netflix.governator.guice.test.ModulesForTesting;
import com.netflix.governator.guice.test.ReplaceWithMock;
import com.netflix.governator.guice.test.WrapWithSpy;

/**
 * An extended {@link BlockJUnit4ClassRunner} which creates a Governator-Guice
 * injector from a list of modules, as well as provides utilities for
 * Mocking/Spying bindings.
 * 
 * See {@link ModulesForTesting}, {@link ReplaceWithMock}, and
 * {@link WrapWithSpy} for example usage.
 */
public class GovernatorJunit4ClassRunner extends BlockJUnit4ClassRunner {
    
    private AnnotationBasedTestInjectorCreator annotationBasedTestInjectorCreator;

    public GovernatorJunit4ClassRunner(Class<?> klass) throws InitializationError {
        super(klass);
        annotationBasedTestInjectorCreator = new AnnotationBasedTestInjectorCreator(klass);
    }

    @Override
    protected Object createTest() throws Exception {
        final Object testInstance = super.createTest();
        annotationBasedTestInjectorCreator.prepareTestFixture(testInstance);
        return testInstance;
    }

    @Override
    protected Statement withAfters(FrameworkMethod method, Object target, Statement statement) {
        final List<FrameworkMethod> afters = getTestClass().getAnnotatedMethods(After.class);
        return new RunAfters(statement, afters, target) {
            @Override
            public void evaluate() throws Throwable {
                super.evaluate();
                annotationBasedTestInjectorCreator.cleanupMocks();
            }
        };
    }

    @Override
    protected Statement withAfterClasses(Statement statement) {
        final List<FrameworkMethod> afters = getTestClass().getAnnotatedMethods(AfterClass.class);
        return new RunAfters(statement, afters, null) {
            @Override
            public void evaluate() throws Throwable {
                super.evaluate();
                annotationBasedTestInjectorCreator.cleanupInjector();
            }
        };
    }
}