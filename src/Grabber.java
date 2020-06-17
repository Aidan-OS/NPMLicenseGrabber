import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

//VERSION 1.0
//LICENSED UNDER MIT LICENSE
//Copyright 2020 CryptoNumerics
//Copyright 2020 Aidan Smith

public class Grabber
{
    public static void main (String[] args)
    {
        String inputFileName = "package.json";
        String outputFileName = "output.csv";
        boolean allDevDeps = false;

        for (int i = 0; i < args.length; i++)
        {
            if (args[i].toLowerCase().equals("alldevdeps")) //Turns on the sorting through all devDeps instead of the normal one layer
            {
                allDevDeps = true;
                System.out.println("WARNING: Recursively sorting through all DevDependencies WILL take a very long time.");
            }
            if (args[i].toLowerCase().contains(".csv"))    //Takes a .csv output file name
            {
                outputFileName = args[i];
                System.out.println("Overwritten output file name to: " + args[i]);
            }
        }

        ArrayList<Package> dependencies = new ArrayList<>();

        String inputDataJSONString = "";

        try //Pull in input dependency data
        {
            File inputFile = new File(inputFileName);
            System.out.println("Getting input file at path: " + inputFile.getAbsolutePath()); //Builds and displays connection to file
            Scanner inputFileReader = new Scanner(inputFile);

            while (inputFileReader.hasNextLine())
            {
                String data = inputFileReader.nextLine();
                data = data.replace("\n", ""); //Reads data from file, removing newlines
                inputDataJSONString += data;
            }
            inputFileReader.close();
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Error: Input file not Found");
            System.exit(1);
        }

        JSONObject inputData = null;

        try
        {
            inputData = (JSONObject) new JSONParser().parse(inputDataJSONString);
        }
        catch (ParseException e)
        {
            System.out.println("Error parsing package.json");
            System.exit(1);
        }

        Map dependenciesJSONMap = (Map) inputData.get("dependencies");

        Iterator<Map.Entry> itr = dependenciesJSONMap.entrySet().iterator();
        while (itr.hasNext())
        {
            Map.Entry pair = itr.next();

            String currentDependency = (String) pair.getKey();
            String currentDependencyVersion = (String) pair.getValue();
            currentDependencyVersion = currentDependencyVersion.replace("^", "");
            currentDependencyVersion = currentDependencyVersion.replace("~", "");

            Package currentDependencyPackage = new Package(currentDependency, currentDependencyVersion, false);

            dependencies.add(currentDependencyPackage);
        }

        Map devDependenciesJSONMap = (Map) inputData.get("devDependencies");

        itr = devDependenciesJSONMap.entrySet().iterator();
        while (itr.hasNext())
        {
            Map.Entry pair = itr.next();

            String currentDependency = (String) pair.getKey();
            currentDependency = currentDependency.replace("\\", "");

            String currentDependencyVersion = (String) pair.getValue();
            currentDependencyVersion = currentDependencyVersion.replace("^", "");
            currentDependencyVersion = currentDependencyVersion.replace("~", "");

            Package currentDependencyPackage = new Package(currentDependency, currentDependencyVersion, true);

            dependencies.add(currentDependencyPackage);
        }

        ArrayList<Package> licensedDependencies = new ArrayList<>(); //These contain all needed information to output
        ArrayList<Package> failedDependencies = new ArrayList<>(); //A lis of all deps that were skipped for any reason

        boolean finished = false;

        while (!finished) //Iterates through the dependencies until no new ones need to be searched
        {
            boolean listTouched = false; //Tells us if the ArrayList was touched in order to determine if we can exit the loop

            ArrayList<Package> newDependencies = new ArrayList<>(); //List to store the newly found dependencies

            for (Package currentDependency : dependencies) //Loop through all dependencies to find their licenses and dependencies
            {
                if (licensedDependencies.contains(currentDependency))
                {
                    System.out.println("Skipping Duplicate: " + currentDependency.getName());
                    continue;
                }

                System.out.println("Currently Checking: " + currentDependency);
                String requestResult = getRequest(currentDependency.getName()); //Gets the JSON from NPMJS
                if (requestResult == null)
                {
                    System.out.println("Failed to get: " + currentDependency); //Skips dependency if no result from NPMJS
                    failedDependencies.add(currentDependency);
                    continue;
                }

                JSONObject dataJSON = null;
                try
                {
                    dataJSON = (JSONObject) new JSONParser().parse(requestResult); //Parses the returned string into a JSON object
                }
                catch (ParseException e)
                {
                    System.out.println("Error: Failed to parse JSON response of " + currentDependency);
                    System.out.println("Skipping " + currentDependency);
                    failedDependencies.add(currentDependency);
                    continue;
                }

                Map address = ((Map) dataJSON.get("versions")); //Gets a map of versions list
                ArrayList<String> versionsList = new ArrayList<>(); //List of strings used to find a matching version to that found in package.json

                // iterating versions Map
                Iterator<Map.Entry> itr1 = address.entrySet().iterator();
                while (itr1.hasNext())
                {
                    Map.Entry pair = itr1.next();

                    versionsList.add((String) pair.getKey());
                }

                if (versionsList.isEmpty())
                {
                    System.out.println("Error: Missing versions in JSON");
                    System.out.println("Skipping " + currentDependency);
                    failedDependencies.add(currentDependency);
                    continue;
                }

                String versionStringToCheck = "";
                if (versionsList.contains(currentDependency.getVersion())) //checks if version found in package.json is in the JSON response
                {
                    versionStringToCheck = currentDependency.getVersion();
                }
                else
                {
                    versionStringToCheck = getGreatestVersion(versionsList);
                    System.out.println("\tNOTICE: Version from Package.json not found, falling back on most recent version");
                }

                System.out.println("\tSearching Version: " + versionStringToCheck); //Prints version for current dependency

                JSONObject versionsJSON = (JSONObject) dataJSON.get("versions"); //Gets the subJSON object of versions
                JSONObject currentVersionInfo = (JSONObject) versionsJSON.get(versionStringToCheck); //Gets info for current version

                String license = "";
                try
                {
                    license = (String) dataJSON.get("license"); //Gets the license of the dependency. Does not use the version info as that does not always have the same format
                }
                catch (ClassCastException e)
                {
                    try
                    {
                        JSONObject licenseObject = (JSONObject) dataJSON.get("license"); //license can sometimes be hidden
                        license = (String) licenseObject.get("type");
                    }
                    catch (ClassCastException e1) //Some people decide to have arrays of license types
                    {
                        JSONArray licenseArray = (JSONArray) dataJSON.get("license");
                        for (Object currentObject : licenseArray)
                        {
                            JSONObject currentJSONObject = (JSONObject) currentObject;
                            license += (String) currentJSONObject.get("type") + ",";
                        }
                        license = license.substring(0, license.length() - 1); //Remove trailing comma
                    }
                }


                try
                {
                    Map subDependenciesMap = (Map) currentVersionInfo.get("dependencies"); //Iterates through all sub dependencies
                    Iterator<Map.Entry> itr2 = subDependenciesMap.entrySet().iterator();
                    while (itr2.hasNext())
                    {
                        Map.Entry pair = itr2.next();
                        String dependency = (String) pair.getKey();
                        dependency = dependency.replace("\\", "");

                        String version = (String) pair.getValue();
                        version = version.replace("^", "");
                        version = version.replace("~", "");

                        Package packageToAdd = new Package(dependency, version, false);

                        if (licensedDependencies.contains(packageToAdd) || dependencies.contains(packageToAdd)) //If this package is anywhere
                        {
                            System.out.println("Skipping Duplicate: " + dependency);
                            continue;
                        }

                        newDependencies.add(packageToAdd);
                    }

                    if (currentDependency.isDevDep())
                    {
                        Map subDevDependenciesMap = (Map) currentVersionInfo.get("devDependencies"); //iterates through all sub dev dependencies
                        Iterator<Map.Entry> itr3 = subDevDependenciesMap.entrySet().iterator();
                        while (itr3.hasNext())
                        {
                            Map.Entry pair = itr3.next();
                            String dependency = (String) pair.getKey();
                            dependency = dependency.replace("\\", "");

                            String version = (String) pair.getValue();
                            version = version.replace("^", "");
                            version = version.replace("~", "");

                            Package packageToAdd = new Package(dependency, version, allDevDeps);
                            //While your product may possibly contain small artifacts of DevDependencies, I highly doubt the existence of their devDependencies in your product.
                            //In whatever case, the command line argument still exists

                            if (licensedDependencies.contains(packageToAdd) || dependencies.contains(packageToAdd)) //If this package is anywhere
                            {
                                System.out.println("Skipping Duplicate: " + dependency);
                                continue;
                            }

                            newDependencies.add(packageToAdd);
                        }
                    }
                }
                catch (NullPointerException e){} //This just means there is no new deps or dev deps

                if (license == null)
                {
                    currentDependency.setLicense("None");
                    System.out.println("\tLicense: None");
                }
                else
                {
                    currentDependency.setLicense(license);
                    System.out.println("\tLicense: " + license);
                }

                licensedDependencies.add(currentDependency);

                listTouched = true;

            }

            dependencies = newDependencies;

            if (listTouched == false)
                finished = true;
        }

        String outputString = "";
        for (Package currentPackage : licensedDependencies) //Iterates through all items in licenses map
        {
            outputString += currentPackage.getName() + ",https://www.npmjs.com/registry/" + currentPackage.getName() + ",code," + currentPackage.getLicense() + ",https://npmjs.com/registry/" + currentPackage.getName() + "\n"; //Builds output CSV string
        }

        String failuresString = "";
        for (Package currentPackage : failedDependencies)
        {
            outputString += currentPackage.getName() + "\n";
        }

        try
        {
            FileWriter outputFileWriter = new FileWriter(outputFileName);
            outputFileWriter.write(outputString);
            outputFileWriter.close();
            System.out.println("Successfully wrote \'output.csv\' to the file");
        }
        catch (IOException e)
        {
            System.out.println("Error: Failed to write \'output.csv\' to file");
        }

        try
        {
            FileWriter outputFileWriter = new FileWriter("SKIPPED DEPENDENCIES");
            outputFileWriter.write(failuresString);
            outputFileWriter.close();
            System.out.println("Successfully wrote \'SKIPPED DEPENDENCIES\' to the file");
        }
        catch (IOException e)
        {
            System.out.println("Error: Failed to write \'SKIPPED DEPENDENCIES\' to file");
        }
    }

    //Fallback for not being able to find the version found in package.json
    private static String getGreatestVersion (ArrayList<String> versionsListString)
    {
        ArrayList<Integer[]> versionsList = new ArrayList<>();

        for (String currentVersion : versionsListString)
        {
            String[] currentVersionArrayString = (currentVersion.split("\\.")); //Splits string up on "." to separate different version numbers
            Integer[] currentVersionArrayInt = new Integer[currentVersionArrayString.length];
            boolean skip = false;

            for (int i = 0; i < currentVersionArrayString.length; i++) //Transforms split string into array of integers
            {
                try
                {
                    currentVersionArrayInt[i] = Integer.parseInt(currentVersionArrayString[i]);
                }
                catch (Exception e) //Means there is characters that arent numbers. This means that the version can be discarded to avoid alpha and beta versions that we dont need to check on fallback.
                {
                    skip = true;
                    break;
                }
            }

            if (skip) //Skips version if needed
            {
                continue;
            }

            versionsList.add(currentVersionArrayInt); //stores array of version numbers
        }

        for (int i = 0; i < 3; i++) //Iterates through major, minor, and patch versions to find the greatest
        {
            int highest = 0;
            for (Integer[] currentVersion : versionsList) //Gets the highest version in the current position (Major, Minor, Patch)
            {
                if (currentVersion[i] > highest)
                {
                    highest = currentVersion[i];
                }
            }

            ArrayList<Integer[]> removeList = new ArrayList<>(); //Generates list of versions to be removed
            for (Integer[] currentVersion : versionsList)
            {
                if (currentVersion[i] < highest)
                {
                    removeList.add(currentVersion);
                }
            }
            versionsList.removeAll(removeList); //Removes older versions of the current position (Major, Minor, Patch)
        }

        Integer[] greatestVersionInt = versionsList.get(0); //Only remaining item in the list is the most recent version
        String greatestVersionString = "";
        for (int i = 0; i < 3; i++)
        {
            greatestVersionString += greatestVersionInt[i] + "."; //Converts to string for use in JSON searching
        }
        greatestVersionString = greatestVersionString.substring(0, greatestVersionString.length() - 1);

        return (greatestVersionString);
    }

    private static String getRequest (String packageName)
    {
        String packageInfo = null;
        String urlString = "https://registry.npmjs.org/" + packageName;
        try
        {
            URL url = new URL (urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");


            if (connection.getResponseCode() != 200)
            {
                System.out.println("Error: Bad response (" + connection.getResponseCode() + ") from URL: " + urlString);
                return (null);
            }

            BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = input.readLine()) != null)
            {
                content.append(inputLine);
            }
            input.close();
            connection.disconnect();

            packageInfo = content.toString();
        }
        catch (MalformedURLException e)
        {
            System.out.println ("Error: Bad URL formed from package name: " + packageName);
            return (null);
        }
        catch (IOException e)
        {
            System.out.println ("Error: Failed to open URL connection to " + urlString);
            return (null);
        }

        return (packageInfo);
    }
}
