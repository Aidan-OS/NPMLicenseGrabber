# README #

### What is this repository for? ###
This repo was built by Aidan Smith in order to more easily aggregate the licenses found in Node JavaScript based projects hosted on NPMJS.

### How do I get set up? ###

* Can be run through commandline (with the jar) or through an editor
* To perform a standard run, put a package.json in the same folder is the code is being run from and run the code!

You can optionally add two command line arguments, Output File Name and AllDevDeps. The order of these does not matter:

* Output File Name must be a .csv file, otherwise it will be skipped and run as normal.
* AllDevDeps is a flag to perform a deeper search into your dev dependencies. This takes a **VERY** long time. To set this command line argument, just add the argument "alldevdeps" in any casing you like.

Example run commands:

Normal Run: `java -jar NPM-License-Grabber-1.0.jar `

Run with custom Output Name: `java -jar NPM-License-Grabber-1.0.jar MyProjectLicenses.csv`

Run with AllDevDeps: `java -jar NPM-License-Grabber-1.0.jar alldevdeps`

Run with both: `java -jar NPM-License-Grabber-1.0.jar alldevdeps MyProjectLicenses.csv`

### Contribution guidelines ###

If you wish to contribute, please submit a pull request clearly explaining the changes made and what problems it solves. I will do my best too look it over quickly.

Please just follow the style set by the work already committed. I know my curlies are different than some, however I find it (and hope you will to) much easier to read and debug.

### Bug Reports ###

Unfortunately this is a side project and I won't have time for major upkeep. If you notice a problem, let me know, and if you are able to fix it, please submit a pull request!