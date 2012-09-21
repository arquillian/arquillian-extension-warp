package org.jboss.arquillian.warp.server.assertion;

import static org.jboss.modules.DependencySpec.createLocalDependencySpec;
import static org.jboss.modules.ResourceLoaderSpec.createResourceLoaderSpec;

import org.jboss.arquillian.warp.server.assertion.TestResourceLoader.TestResourceLoaderBuilder;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleSpec;
import org.jboss.modules.filter.ClassFilter;
import org.jboss.modules.filter.ClassFilters;
import org.jboss.modules.filter.PathFilter;
import org.jboss.modules.filter.PathFilters;
import org.jboss.modules.util.ModulesTestBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

public class SeparateClassloaderTestRunner extends BlockJUnit4ClassRunner {

    private static Base base = new Base();

    public SeparateClassloaderTestRunner(Class<?> clazz) throws InitializationError {
        super(getFromTestClassloader(clazz));
    }

    private static Class<?> getFromTestClassloader(Class<?> clazz) throws InitializationError {
        try {

            final ModuleIdentifier identifier = ModuleIdentifier.create("module-identifier");
            ModuleSpec.Builder specBuilder = ModuleSpec.build(identifier);
            
//            String packagePath = TestDynamicClassLoading.class.getPackage().getName().replace('.', '/');
//            PathFilter inA = PathFilters.match(packagePath + "/TestDynamicClassLoading.class");
//            PathFilter inB = PathFilters.match(packagePath + "/BarImpl.class");
//            PathFilter exA = PathFilters.match(packagePath + "/QuxImpl.class");

//            PathFilter in = PathFilters.any(inA, inB);
//            PathFilter ex = PathFilters.not(PathFilters.any(exA));
//            final PathFilter filter = PathFilters.all(in, ex);
            
            PathFilter in = PathFilters.acceptAll();
            
            ClassFilter classImportFilter = ClassFilters.acceptAll();
            ClassFilter classExportFilter = ClassFilters.fromResourcePathFilter(in);
            
            PathFilter importFilter = PathFilters.acceptAll();
            PathFilter exportFilter = PathFilters.acceptAll();
            PathFilter resourceImportFilter = PathFilters.acceptAll();
            PathFilter resourceExportFilter = PathFilters.acceptAll();
            
            specBuilder.addResourceRoot(createResourceLoaderSpec(getTestResourceLoader()));
            specBuilder.addDependency(createLocalDependencySpec(importFilter, exportFilter, resourceImportFilter, resourceExportFilter, classImportFilter, classExportFilter));
            base.addModuleSpec(specBuilder.create());

            return base.loadClass(identifier, clazz.getName());

        } catch (Exception e) {
            e.printStackTrace();
            throw new InitializationError(e);
        }
    }
    
    private static TestResourceLoader getTestResourceLoader() throws Exception {
        TestResourceLoaderBuilder builder = new TestResourceLoaderBuilder();
        builder.addClasses(TestDynamicClassLoading.class);
        return builder.create();
    }

    private static class Base extends ModulesTestBase {

        public Base() {
            try {
                setUp();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public Class<?> loadClass(ModuleIdentifier identifier, String className) throws Exception {
            System.out.println("loadclass: " + className);
            return super.loadClass(identifier, className);
        }
        
        @Override
        protected void addModuleSpec(ModuleSpec moduleSpec) {
            super.addModuleSpec(moduleSpec);
        }
    }
}