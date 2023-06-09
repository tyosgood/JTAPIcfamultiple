# JTAPI Light Control

## Overview

Proof of concept using JTAPI and an IP Phone Service to allow a user to easily forward multiple lines from their phone.

The program reads the contents of a CSV file (extensionList.csv) with the extensions to be forwarded. The user then initiates the forward from an IP phone service on their phone - that service must be defined and subscribed on CUCM. The address for the service should be pointed to the address of the container running this code http://<IP>:<port>/cfaMultiple.  By default the port is 4567.

This functions independently of the CFwdALL button on the phone, but if the the primary line on the phone is in the list of extensions to be forwarded, pressing the CFwdALL softkey will cancel the forwarding only on the primary line. In this scenario, all the other lines in the list would stay forwarded, but there would be no visual indication on the phone.



Visit the [DevNet JTAPI Site](https://developer.cisco.com/site/jtapi)


## Requirements

- [OpenJDK 8](https://openjdk.java.net/)

- [Apache Maven](https://maven.apache.org/) 3.6.3

- [Visual Studio Code](https://code.visualstudio.com/) with the [MS Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)

- A working Cisco Unified Communications Manager environment:

    - An CUCM application-user or end-user username/password, with the following roles:

        - `Standard CTI Enabled`

        - `Standard CTI Allow Control of all Devices`

        - `Control of the device to be forwarded`



**Tested With:**

* Ubuntu 22.04
* Windows 10 / 11
* OpenJDK 8 / 11
* Maven 3.6.3
* CUCM 11.5 / 12.5 / 14

## Getting started

1. Make sure you have OpenJDK 11 installed, `java` is available in the path, and `$JAVA_HOME` points to the right directory:

    ```bash
    $ java -version
    openjdk version "1.8.0_342"
    OpenJDK Runtime Environment (build 1.8.0_342-8u342-b07-0ubuntu1~22.04-b07)
    OpenJDK 64-Bit Server VM (build 25.342-b07, mixed mode)
    ```

    ```bash
    $ echo $JAVA_HOME
    /usr/lib/jvm/java-8-openjdk-amd64
    ```

1. Open a terminal and use `git` to clone this repository:

    ```bash
    git clone https://github.com/tyosgood/JTAPI-LightControl.git
    ```

1. Open the Java project in [Visual Studio Code](https://code.visualstudio.com/):

    ```bash
    cd jtapi-samples
    code .
    ```

1. Configure the Java runtime for the project (see [Configure Java Runtime](https://code.visualstudio.com/docs/java/java-project#_configure-runtime-for-projects)):

   Open (or create) `.vscode/settings.json`

   **Sample configuration**

   ```json
   {
       "java.configuration.runtimes": [
           {
               "name": "JavaSE-1.8",
               "path": "/usr/lib/jvm/java-8-openjdk-amd64"
           }
       ]
   }   
   ```

1. Edit rename `.env.example` to `.env`, and edit to specify environment variable config for the samples you wish to run.

1. Finally, to launch the sample in VS Code, select the **Run** panel, choose the desired `Launch...` option from the drop-down in the upper left, and click the green 'Start Debugging' arrow (or hit **F5**)

    ![Launch](images/launch.png)

## Notes

1. In this project, the 11.5, 12.5 and 14 versions of the JTAPI Java library have been deployed to the project's local Maven repo (in `lib/`), with 14 being the configured version. 

    If you want to use another deployed version, modify `pom.xml` to specify the desired JTAPI version dependency.  Modify `<version>`:

    ```xml
    <dependency>
        <groupId>com.cisco.jtapi</groupId>
        <artifactId>jtapi</artifactId>
        <version>12.5</version>
    </dependency>
    ```

1.  If  you want to deploy another JTAPI version in the project:

    * Download and install/extract the JTAPI plugin from CUCM (**Applications** / **Plugins**)

    * From this repository's root, use Maven to deploy the new version of `jtapi.jar` to the local repo.  You will need to identify the full path to the new `jtapi.jar` installed above:

        ```bash
        mvn deploy:deploy-file -DgroupId=com.cisco.jtapi -DartifactId=jtapi -Dversion={version} -Durl=file:./lib -DrepositoryId=local-maven-repo -DupdateReleaseInfo=true -Dfile={/path/to/jtapi.jar}
        ```

        >Note: be sure to update {version} and {/path/to/jtapi.jar} with your actual values

1. JTAPI configuration - e.g. trace log number/size/location and various timeouts - can be configured in `jtapi_config/jtapi.ini` (defined as a resource in `pom.xml`)

1. As of v14, the Cisco `jtapi.jar` does not implement the [Java Platform Module System](https://www.oracle.com/corporate/features/understanding-java-9-modules.html) (JPMS).  See this [issue](https://github.com/CiscoDevNet/jtapi-samples/issues/1) for more info.
