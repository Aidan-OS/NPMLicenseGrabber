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

Please just follow the style set by the work already committed. I know my curlies are different than some, however I find it (and hope you will to) much easier to read and debug.

### Who do I talk to? ###

* Want to know why it was made? Talk to Jimmy.
* Want quick help with Java? Talk to whoever is working on Protect-Core
* Really confused about what is going on? Talk to me, Aidan, if you can reach me.