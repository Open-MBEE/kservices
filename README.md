# kservices
Query evaluation and problem solving services in K

The [k](https://github.com/Open-MBEE/kservices/blob/develop/k) script. will run solver
services from the command line.  It assumes that the this repository is cloned in a
`$HOME/git` directory, that maven and java are installed, and that the `develop`
branches following repositories are also checked out into `$HOME/git`:
  * [K](https://github.com/Open-MBEE/K/tree/develop)
  * [bae](https://github.com/Open-MBEE/bae/tree/develop)
  * [util](https://github.com/Open-MBEE/util/tree/develop)
  * [sysml](https://github.com/Open-MBEE/sysml/tree/develop)
  
A web-based IDE for K is slowly under construction:
[k_webeditor](https://github.jpl.nasa.gov/mbee-dev/k_webeditor).

These websites may be available to try out: 
  * http://theklanguage.com (server from the K repository; has documentation, but 
    running may be broken.)
  * http://flipper.jpl.nasa.gov:8080/ (the IDE under construction; JPL only; behavior
    and Java extensions; no documenation)

### Set up
  1. Install git, maven, and Java 1.8.
  1. Clone the develop branches of kservices, bae, and klang into the same directory.   
  3. Set the `JAVA_HOME` environment variable to the installation directory of Java 1.8.
     For Mac OS, the path ends in `Content/Home`.
  2. To build the repositories from a terminal, in each of the \[`util`, `sysml`,\]
     `klang` and `bae` directories, enter
  
    mvn clean package
    
  5. In the `kservices` directory, enter
  
    mvn clean package -Pmbee-dev
  
  * JUnit tests are run after compiling Java.  They should all pass.
    
  * If there are problems getting the util and sysml repositories, clone those into the
     same directory as the others.  You may also need to change the pom.xml files in bae,
     klang, and kservices to find the jars for util and sysml at 
     ../{util|sysml}/target/???-SNAPSHOT???.jar.  You should be able to see examples of 
     this for getting the bae and klang jars from 
  5. If having trouble with microsoft z3 integration, you may need to set `LD_LIBRARY_PATH`
     to point to `klang/lib`.  On Mac OS, it is `DYLIB_LIBRARY_PATH`.  If using Eclipse or
     Intellij IDEA, you can add this variable to the run configuration(s).  These may
     already be included in the `k` script.
  6. If you want to use GLPK for a linear programming problem, you must first install GLPK v4.65 and compile the C++ code that links with it. The tarball can be downloaded from https://www.gnu.org/software/glpk/. After extracting its contents, run `make install`. Then, in the `src/gov/nasa/jpl/kservices/callGLPK` directory, run `g++ -c callGLPK.cpp`, then `g++ callGLPK.o -lglpk -lm -o callGLPK`.
 
 ### Run
   * To test out the z3 solver on k files, you can run the `k` script from a
     terminal in the kservices directory like this:
     
    ./k --smt --captureOff src/kTestCases/simple.k

   * To test out the bae solver on k files, you can run the `k` script from a
     terminal in the kservices directory like this:
        
    ./k --solve --captureOff src/kTestCases/simple.k
  
   * Full output is normally suppressed.  The `--captureOff` option lets you see most
     of it.  Leave out the option if you want simpler output in json format.
   * Output files with the suppressed output can be found in the directory `generatedSrc/<pkg>`,
     where `<package>` is a package name specified for the run.  The `--package <pkg>` option
     specifies this; else the package defaults to `generatedCode`.  
   * To learn how to run using the `mvn` command, see the `mvn` command in the `k` script.
   * To skip JUnit tests with the mvn command, add `-DskipTests=true` to the arguments.
   * A webserver for accessing these services
  
  ### Troubleshooting

   * See if the `MAVEN_OPTS` environment variable is set.  Options specified in `MAVEN_OPTS`
     are included with others to `mvn`.
   * Sometimes it helps to tell maven where to collect libraries:
   
    export M2_REPO=${HOME}/.m2/repository

  ###### The End
 
