package org.IAP491G3.Agent.AgentCore;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Filter {
    static final int HIGH_SCORE = 3;
    static final int MEDIUM_SCORE = 2;
    static final int LOW_SCORE = 1;
    static final Pattern longStringPattern = Pattern.compile("=(.{100,});");
//    static final List<String> riskPackage = Arrays.asList("net.rebeyond.", "com.metasploit.");
    static int riskScore ;

    // Define risky java reflection
    static final List<String> riskReflection = Arrays.asList(
            ".getDeclaredMethod(",
            ".getMethod(",
            ".invoke(",
            "defineClass"
    );
    // Define encryption and encoding
    static final List<String> riskCrypto = Arrays.asList(
            "java.security",
            "javax.crypto"
    );
    static final List<String> riskBase64 = Arrays.asList(
            "java.util.Base64",
            "sun.misc.BASE64Decoder"
    );
    // Define risky java sign new filter, listener, servlet, controller, interceptor
    static final List<String> signFilter = Arrays.asList(
            "setFilterName(",
            "addFilter(",
            "setFilterClass("
    );

    static final List<String> signListener = Arrays.asList(
            "addApplicationEventListener(",
            "addListener("
    );

    static final List<String> signServlet = Arrays.asList(
            "addServletMapping(",
            "addServletMappingDecoded(",
            "ServletRegistration.Dynamic",
            "ApplicationServletRegistration",
            "addServlet("
    );

    static final List<String> signController = Arrays.asList(
            "registerMapping(",
            "registerHandler("
    );

    static final List<String> signInterceptor = Arrays.asList(
            "getDeclaredField(\"adaptedInterceptors"
    );

    // Define risky keyword to execute command
    static final List<String> riskKeyword = Arrays.asList(
            "getRuntime",
            "exec",
            "ProcessBuilder",
            "/bash",
            "/sh",
            "cmd",
            "powershell"
    );



    //=================================================================================
    public static Map<String, List<String>> filterBlackList(String content, String className, Boolean checkComponent) {
        Scanner scanner = new Scanner(content);
         riskScore=0;


        boolean riskFound = false;
        Map<String, List<String>> matchedRule = new HashMap<>();
        matchedRule.put("Package", new ArrayList<>());
        matchedRule.put("Filter", new ArrayList<>());
        matchedRule.put("Listener", new ArrayList<>());
        matchedRule.put("Servlet", new ArrayList<>());
        matchedRule.put("Controller", new ArrayList<>());
        matchedRule.put("Interceptor", new ArrayList<>());
        matchedRule.put("Reflection", new ArrayList<>());
        matchedRule.put("RiskyKeyword", new ArrayList<>());
        matchedRule.put("RiskBase64", new ArrayList<>());
        matchedRule.put("LongLine", new ArrayList<>());
        matchedRule.put("RiskCrypto", new ArrayList<>());
        matchedRule.put("ClassName", new ArrayList<>());
        matchedRule.put("RiskScore", new ArrayList<>());

        try {
            while (scanner.hasNextLine() && !riskFound) {
                String line = scanner.nextLine();

//                // Check for risky package
//                if (line.startsWith("package")) {
//                    String packageName = line.replace("package ", "").replace(";", "").trim();
//                    for (String packagePrefix : riskPackage) {
//                        if (packageName.startsWith(packagePrefix)) {
//                            riskScore+=5;
//                            matchedRule.get("Package").add(packagePrefix);
//                            System.out.println("Found risky package in: " + className + " - Risk Point: " +riskScore);
//                            riskFound = true;
//                            break;
//                        }
//                    }
//                }

                // Check for long strings exceeding 100 characters using regex pattern: =(.{100,});"
                Matcher matcher = longStringPattern.matcher(line);
                if (matcher.find() && matchedRule.get("LongLine").isEmpty()) {
                    String matchedString = matcher.group(1).trim();
                    if (! matchedString.contains("_jspxFactory")){
                        riskScore+=1;
                        matchedRule.get("LongLine").add(matchedString);
                    }
                }
                // Check for encryption, encoding, risk exec keyword
                checkRisk(line, riskKeyword, "RiskyKeyword", matchedRule, HIGH_SCORE,  true);
                checkRisk(line, riskBase64, "RiskBase64", matchedRule, MEDIUM_SCORE,  true);
                checkRisk(line, riskCrypto, "RiskCrypto", matchedRule, MEDIUM_SCORE,  true);

                // Check for dynamic invoke class (java reflection)
                checkRisk(line, riskReflection, "Reflection", matchedRule, MEDIUM_SCORE, true);
                // Check for risky component registration (Filters, Listeners, Servlets, Controllers, Interceptors)

                if (checkComponent) {
                    checkComponent = !checkRisk(line, signFilter, "Filter", matchedRule, HIGH_SCORE,  checkComponent);
                    checkComponent = !checkRisk(line, signListener, "Listener", matchedRule, HIGH_SCORE,  checkComponent);
                    checkComponent = !checkRisk(line, signServlet, "Servlet", matchedRule, HIGH_SCORE,  checkComponent);
                    checkComponent = !checkRisk(line, signController, "Controller", matchedRule, HIGH_SCORE,  checkComponent);
                    checkComponent = !checkRisk(line, signInterceptor, "Interceptor", matchedRule, HIGH_SCORE,  checkComponent);
                }
            }

        } catch (Exception e) {
            System.out.println(e.getCause());
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }

        matchedRule.get("RiskScore").add(String.valueOf(riskScore));
        matchedRule.get("ClassName").add(className);
        System.out.println("Analysis complete. Results written to log file.");
        return matchedRule;
    }

    private static boolean checkRisk(String line, List<String> patterns, String riskType, Map<String, List<String>> matchedRule, int addPoint, boolean checkComponent) {
        boolean riskFound = false;
        if (!checkComponent) {
            return riskFound;
        }
        for (String pattern : patterns) {
            if (line.contains(pattern)) {
                riskFound = true;
                List<String> tempRiskType = matchedRule.get(riskType);
                if (!tempRiskType.isEmpty()) {
                    return riskFound;

                }
                matchedRule.get(riskType).add(pattern);
                riskScore += addPoint;
            }
        }
        return riskFound;
    }
}
