package org.eclipse.scout.migration.ecma6.context;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.migration.ecma6.Configuration;
import org.eclipse.scout.migration.ecma6.FileUtility;
import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.model.api.ApiParser;
import org.eclipse.scout.migration.ecma6.model.api.ApiWriter;
import org.eclipse.scout.migration.ecma6.model.api.INamedElement;
import org.eclipse.scout.migration.ecma6.model.api.Libraries;
import org.eclipse.scout.migration.ecma6.model.api.less.LessApiParser;
import org.eclipse.scout.migration.ecma6.model.old.JsClass;
import org.eclipse.scout.migration.ecma6.model.old.JsFile;
import org.eclipse.scout.migration.ecma6.model.old.JsFileParser;
import org.eclipse.scout.migration.ecma6.pathfilter.IMigrationExcludePathFilter;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Context {
  private static final Logger LOG = LoggerFactory.getLogger(Context.class);


  private final Map<Path, WorkingCopy> m_workingCopies = new HashMap<>();
  private final Map<WorkingCopy, JsFile> m_jsFiles = new HashMap<>();
  private final Map<String /*fqn*/, JsClass> m_jsClasses = new HashMap<>();
  private Libraries m_libraries;
  private INamedElement m_api;
  private LessApiParser m_lessApi;

  public void setup() {
    try {
      readLibraryApis();
    }
    catch (IOException e) {
      throw new ProcessingException("Could not parse Library APIs in '" + Configuration.get().getLibraryApiDirectory() + "'.", e);
    }
    try {
      parseJsFiles();
      setupCurrentApi();
      parseLessFiles();
    }
    catch (IOException e) {
      throw new ProcessingException("Could not parse Files.", e);
    }
    // setup context properties
    BEANS.all(IContextProperty.class).forEach(p -> p.setup(this));
  }

  protected void parseLessFiles() throws IOException {
    Configuration config = Configuration.get();
    LessApiParser lessApi = new LessApiParser();
    lessApi.setName(config.getPersistLibraryName());
    lessApi.parseFromSourceDir(config.getSourceModuleDirectory(), this);
    lessApi.parseFromLibraries(m_libraries);
    m_lessApi = lessApi;
  }

  protected void setupCurrentApi() {
    ApiWriter writer = new ApiWriter();
    m_api = writer.createLibraryFromCurrentModule(Configuration.get().getNamespace(), this, false);
  }

  protected void readLibraryApis() throws IOException {
    Path libraryApiDirectory = Configuration.get().getLibraryApiDirectory();
    if (libraryApiDirectory != null) {
      ApiParser parser = new ApiParser(libraryApiDirectory);
      m_libraries = parser.parse();
    }
    else {
      m_libraries = new Libraries();
    }
  }

  public WorkingCopy getWorkingCopy(Path file) {
    return m_workingCopies.get(file);
  }

  public WorkingCopy newFile(Path file) {
    WorkingCopy wc = createWorkingCopy(file, "\n");
    wc.setSource("");
    return wc;
  }

  public WorkingCopy ensureWorkingCopy(Path file) {
    return createWorkingCopy(file, FileUtility.lineSeparator(file));
  }

  protected WorkingCopy createWorkingCopy(Path file, String nl) {
    return m_workingCopies.computeIfAbsent(file, p -> new WorkingCopy(p, nl));
  }

  public Collection<WorkingCopy> getWorkingCopies() {
    return m_workingCopies.values();
  }

  public <VALUE> VALUE getProperty(Class<? extends IContextProperty<VALUE>> propertyClass) {
    return BEANS.get(propertyClass).getValue();
  }

  public JsClass getJsClass(String fullyQualifiedName) {
    return m_jsClasses.get(fullyQualifiedName);
  }


  public Path relativeToModule(Path path) {
    Assertions.assertNotNull(Configuration.get().getSourceModuleDirectory());
    return path.relativize(Configuration.get().getSourceModuleDirectory());
  }

  public JsFile ensureJsFile(WorkingCopy workingCopy) {
    JsFile file = m_jsFiles.get(workingCopy);
    if (file == null) {
      try {
        file = new JsFileParser(workingCopy).parse();
        m_jsFiles.put(workingCopy, file);
      }
      catch (IOException e) {
        throw new VetoException("Could not parse working copy '" + workingCopy + "'.", e);
      }
    }
    return file;
  }

  public LessApiParser getLessApi() {
    return m_lessApi;
  }

  public INamedElement getApi() {
    return m_api;
  }

  public Libraries getLibraries() {
    return m_libraries;
  }

  protected void parseJsFiles() throws IOException {

    final Path src = BEANS.get(Configuration.class).getSourceModuleDirectory().resolve("src/main/js");
    if (!Files.exists(src) || !Files.isDirectory(src)) {
      LOG.info("Could not find '" + src + "' to parse js files.");
      return;
    }
    Files.walkFileTree(src, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        if (dir.endsWith(Paths.get("src/main/js/jquery"))) {
          return FileVisitResult.SKIP_SUBTREE;
        }
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        if (!FileUtility.hasExtension(file, "js")) {
          return FileVisitResult.CONTINUE;
        }
        PathInfo info = new PathInfo(file, Configuration.get().getSourceModuleDirectory());
        if (BEANS.all(IMigrationExcludePathFilter.class).stream().anyMatch(filter -> filter.test(info))) {
          return FileVisitResult.CONTINUE;
        }
        JsFile jsClasses = ensureJsFile(ensureWorkingCopy(file));
        jsClasses.getJsClasses().forEach(jsClazz -> m_jsClasses.put(jsClazz.getFullyQualifiedName(), jsClazz));

        return FileVisitResult.CONTINUE;
      }
    });
  }

  public Collection<JsClass> getAllJsClasses() {
    return Collections.unmodifiableCollection(m_jsClasses.values());
  }
}
