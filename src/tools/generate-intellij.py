#! /usr/bin/env python

import os
import os.path
import subprocess
import shutil

LIBRARY_JAR_ROOT = os.path.join('bazel-genfiles', 'external')
BUILD_EVERYTHING_COMMAND = ['bazel', 'build', 'src/...']
PROTO_JAR_ROOT = os.path.join('bazel-bin', 'src', 'main', 'proto')

PROJECT_NAME = 'Polyglot'

IDEA_TEMPLATE = 'src/tools/idea-template'
IDEA_TARGET = '.idea'

# This directory will contain the set of xml files describing the library dependencies
LIBRARY_DIR = os.path.join(IDEA_TARGET, 'libraries')

# This path must match the module in modules.xml
IML_FILE = 'src/project.iml'


def main():
    # Using relative paths for certain things makes our lives much easier, but
    # this requires being run from the root of the Bazel workspace.
    if not os.path.isfile(os.path.join(os.getcwd(), 'WORKSPACE')):
        print('This script must be invoked from the WORKSPACE root.')
        return

    # Build the project to make sure all jars are present.
    print('Building project...')
    subprocess.check_output(BUILD_EVERYTHING_COMMAND)

    # Copying project template...
    if os.path.exists(IDEA_TARGET):
        print('Removing existing ' + IDEA_TARGET + ' directory...')
        shutil.rmtree(IDEA_TARGET)

    print('Copying templates to ' + IDEA_TARGET + ' directory...')
    shutil.copytree(IDEA_TEMPLATE, IDEA_TARGET)

    print('Setting project name to ' + PROJECT_NAME)
    with open(os.path.join(IDEA_TARGET, '.name'), 'w') as file:
        file.write(PROJECT_NAME)

    print('Generating libraries files...')
    jars = discover_jars(LIBRARY_JAR_ROOT)
    os.mkdir(LIBRARY_DIR)
    order_entries = []
    for jar in jars:
        # Strip off .jar and replace with .xml
        jar_name = os.path.basename(jar)[:-4]
        jar_path = 'jar://$PROJECT_DIR$' + jar[len(os.getcwd()):]
        with open(os.path.join(LIBRARY_DIR, jar_name + '.xml'), 'w') as file:
            file.write(LIBRARY_TEMPLATE.format(library_name=jar_name, library_path=jar_path))

        # Jar entries in the main.iml & test.iml files
        order_entries.append(
            '    <orderEntry type="library" name="{library_name}" level="project" />'
            .format(library_name=jar_name))

    with open(IML_FILE, 'w') as file:
        file.write(IML_TEMPLATE.format(order_entries="\n".join(order_entries)))

    print('Done')


def discover_jars(jar_root):
    jar_paths = []

    trees = [LIBRARY_JAR_ROOT, PROTO_JAR_ROOT]
    for tree in trees:
        for jar_root, _, files in os.walk(tree):
            for file in files:
                if os.path.splitext(file)[1] == '.jar':
                    jar_paths.append(os.path.abspath(os.path.join(jar_root, file)))
    return jar_paths

LIBRARY_TEMPLATE = """<component name="libraryTable">
  <library name="{library_name}">
    <CLASSES>
      <root url="{library_path}!/" />
    </CLASSES>
    <JAVADOC />
    <SOURCES />
  </library>
</component>
"""

IML_TEMPLATE = """<?xml version="1.0" encoding="UTF-8"?>
<module type="JAVA_MODULE" version="4">
  <component name="NewModuleRootManager" inherit-compiler-output="true">
    <exclude-output />
    <content url="file://$MODULE_DIR$">
      <sourceFolder url="file://$MODULE_DIR$/main" isTestSource="false" />
      <sourceFolder url="file://$MODULE_DIR$/test" isTestSource="true" />
    </content>
    <orderEntry type="inheritedJdk" />
    <orderEntry type="sourceFolder" forTests="false" />
    {order_entries}
  </component>
</module>
"""

if __name__ == '__main__':
    main()
