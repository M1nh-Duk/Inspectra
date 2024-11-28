package org.IAP491G3.Agent.AgentCore;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Filter {
    static final int HIGH_SCORE = 3;
    static final int MEDIUM_SCORE = 2;
    static final int LOW_SCORE = 1;
    static final Pattern longStringPattern = Pattern.compile("=(.{100,});");
    static final List<String> riskPackage = Arrays.asList("net.rebeyond.", "com.metasploit.");
    static int riskScore ;
    // Define risky java reflection
    static final List<String> riskReflection = Arrays.asList(
            ".getDeclaredMethod(",
            ".getMethod(",
            ".invoke(",
            "defineClass"
    );
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
            "setFilterName",
            "addFilter("
    );

    static final List<String> signListener = Arrays.asList(
            "addApplicationEventListener(",
            "addListener("
    );

    static final List<String> signServlet = Arrays.asList(
            "addServletMapping(",
            "addServletMappingDecoded(",
            "ServletRegistration.Dynamic", //interface servlet tomcat => cho phép đăng ký mới một servlet
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
            ".getRuntime().exec(",
            "ProcessBuilder",
            "/bin/bash",
            "/bin/sh",
            "cmd",
            "powershell"
    );

/*
    public static void filterBlackList(String content, String className) throws IOException {


        String regex = "\"([^\"]+)\"";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);

        boolean shouldAdd = false;
        int riskPoint = 0;
        try {
            // Check risk package
            if (content.contains("package ")) {
                String packageName = content.split("\n")[0].replace("package ", "").replace(";", "").trim();
                for (String packagePrefix : riskPackage) {
                    if (packageName.startsWith(packagePrefix)) {
                        shouldAdd = true;
                        riskPoint += 5;
                        System.out.println("Found risky package in: " + className + " - Risk Point: " + riskPoint);
                        break;
                    }
                }
            }


            //check risky keyword
            if (!shouldAdd) {  // Chỉ kiểm tra keyword nếu chưa đánh dấu là risky
                for (String keyword : riskKeyword) {
                    if (content.contains(keyword)) {
                        shouldAdd = true;
                        riskPoint += 5;
                        System.out.println("Found risky behaviour in: " + className + " - Risk Point: " + riskPoint);
                        break;
                    }
                }
            }


            // Kiểm tra và in ra các chuỗi có độ dài lớn hơn 100 ký tự
            List<String> longStrings = new ArrayList<>();
            while (matcher.find()) {
                String str = matcher.group(1); // Lấy chuỗi trong dấu ngoặc kép
                if (str.length() > 100) {
                    longStrings.add(str);
                }
            }
            if (!longStrings.isEmpty()) {
                riskPoint += 1;
                System.out.println("Found large String in: " + className + " - Risk Point: " + riskPoint);
            }

            //Kiểm tra sử dụng base64lib
            if (content.contains("sun.misc.BASE64Decoder") || content.contains("java.util.Base64")) {
                riskPoint += 1;
                System.out.println("Found the use of base64 lib in the file: " + className + " - Risk Point: " + riskPoint);
            }


//          Check dynamic invoke class (java reflection)
            if (!shouldAdd) {
                boolean allKeywordsFound = true;
                for (String reflection : riskReflection) {
                    if (!content.contains(reflection)) {
                        allKeywordsFound = false;
                        break;
                    }
                }
                if (allKeywordsFound) {
                    riskPoint += 2;
                    boolean filterFound = false;
                    boolean listenerFound = false;
                    boolean servletFound = false;
                    boolean controllerFound = false;
                    boolean interceptorFound = false;

                    //Kiểm tra Filter
                    if (checkRiskFilter(content, className)) {
                        riskPoint += 1;
                        shouldAdd = true;
                        filterFound = true;
                        System.out.println("Found risky sign new Filter action in file: " + className + " - riskPoint: " + riskPoint);
                    }

                    // Kiểm tra Listener
                    if (checkRiskListener(content, className)) {
                        riskPoint += 1;
                        shouldAdd = true;
                        listenerFound = true;
                        System.out.println("Found risky sign new Listener action in file: " + className + " - riskPoint: " + riskPoint);
                    }

                    // Kiểm tra  Servlet
                    if (checkRiskServlet(content, className)) {
                        riskPoint += 1;
                        shouldAdd = true;
                        servletFound = true;
                        System.out.println("Found risky sign new Servlet action in file: " + className + " - riskPoint: " + riskPoint);
                    }

                    //Kiểm tra Spring Boot Controller
                    if (checkRiskController(content, className)) {
                        riskPoint += 1;
                        shouldAdd = true;
                        controllerFound = true;
                        System.out.println("Found risky sign new Controller action in file: " + className + " - riskPoint: " + riskPoint);
                    }

                    //Kiểm tra Spring Boot Interceptor
                    if (checkRiskInterceptor(content, className)) {
                        riskPoint += 1;
                        shouldAdd = true;
                        interceptorFound = true;
                        System.out.println("Found risky sign new Interceptor action in file: " + className + " - riskPoint: " + riskPoint);
                    }

                    //Đánh dấu file chỉ dynamical loading class
                    if (!filterFound && !listenerFound && !servletFound && !interceptorFound && !controllerFound) {
                        System.out.println("File dynamical loading class via java reflection: " + className + " - Risk Point: " + riskPoint);
                        shouldAdd = true;
                    }
                }

            }


            // Log the results
            System.out.println("============================================================ FILTER RESULT");
            System.out.println("Class: " + className + "|| Point: " + riskPoint);
            System.out.println("Analysis complete. Results written to log file.");
        } catch (Exception e) {
            System.out.println(e.getCause());
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    */

    //=================================================================================
    public static Map<String, List<String>> filterBlackList(String content, String className, Boolean checkComponent) {
        Scanner scanner = new Scanner(content);
         riskScore=0;

//        Pattern longStringPattern = Pattern.compile("\"([^\"]{100,})\"");

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

                // Check for risky package
                if (line.startsWith("package")) {
                    String packageName = line.replace("package ", "").replace(";", "").trim();
                    for (String packagePrefix : riskPackage) {
                        if (packageName.startsWith(packagePrefix)) {
                            riskScore+=5;
                            matchedRule.get("Package").add(packagePrefix);
                            System.out.println("Found risky package in: " + className + " - Risk Point: " +riskScore);
                            riskFound = true;
                            break;
                        }
                    }
                }

                // Check for risky keywords
//                for (String keyword : riskKeyword) {
//                    if (line.contains(keyword)) {
//                        riskScore += 5;
//                        System.out.println("Found risky behaviour in: " + className + " - Risk Point: " + riskScore);
//                        break;
//                    }
//                }
                // Check for long strings
                Matcher matcher = longStringPattern.matcher(line);
                if (matcher.find()) {
                    String matchedString = matcher.group(1).trim();
                    if (! matchedString.contains("_jspxFactory")){
                        riskScore+=1;
                        matchedRule.get("LongLine").add(matchedString);
                    }
                }
                // Check for encryption, encoding, risk exec keyword
                checkRisk(line, riskKeyword, "RiskyKeyword", matchedRule, HIGH_SCORE, true, true);
                checkRisk(line, riskBase64, "RiskBase64", matchedRule, LOW_SCORE, true, true);
                checkRisk(line, riskCrypto, "RiskCrypto", matchedRule, MEDIUM_SCORE, true, true);

                // Check for dynamic invoke class (java reflection)
                checkRisk(line, riskReflection, "Reflection", matchedRule, MEDIUM_SCORE, true, true);
                // Check for risky component registration (Filters, Listeners, Servlets, Controllers, Interceptors)

                if (checkComponent) {
                    checkComponent = !checkRisk(line, signFilter, "Filter", matchedRule, HIGH_SCORE, false, checkComponent);
                    checkComponent = !checkRisk(line, signListener, "Listener", matchedRule, HIGH_SCORE, false, checkComponent);
                    checkComponent = !checkRisk(line, signServlet, "Servlet", matchedRule, HIGH_SCORE, false, checkComponent);
                    checkComponent = !checkRisk(line, signController, "Controller", matchedRule, HIGH_SCORE, false, checkComponent);
                    checkComponent = !checkRisk(line, signInterceptor, "Interceptor", matchedRule, HIGH_SCORE, false, checkComponent);
                    checkComponent = !checkRisk(line, signFilter, "Servlet", matchedRule, HIGH_SCORE, false, checkComponent);
                }


//                if (!shouldAdd) {
//                    boolean allKeywordsFound = true;
//                    for (String reflection : riskReflection) {
//                        if (!line.contains(reflection)) {
//                            allKeywordsFound = false;
//                            break;
//                        }
//                    }
//                    if (allKeywordsFound) {
//                        riskScore += 2;
//                        shouldAdd = true;
//                        System.out.println("Found risky reflection action in file: " + className + " - Risk Point: " + riskScore);
//                    }
//                }

            }

        } catch (Exception e) {
            System.out.println(e.getCause());
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
        // Log the results
//        System.out.println("============================================================ FILTER RESULT");
//        System.out.println("Class: " + className + "|| Point: " +riskScore);
        matchedRule.get("RiskScore").add(String.valueOf(riskScore));
        matchedRule.get("ClassName").add(className);
        System.out.println("Analysis complete. Results written to log file.");
        return matchedRule;
    }

    private static boolean checkRisk(String line, List<String> patterns, String riskType, Map<String, List<String>> matchedRule, int addPoint, boolean searchAllPattern, boolean checkComponent) {
        boolean riskFound = false;
        if (!checkComponent) {
            return riskFound;
        }
        for (String pattern : patterns) {
            if (line.contains(pattern)) {
                riskFound = true;
                matchedRule.get(riskType).add(pattern);
                 riskScore+=addPoint;
                if (!searchAllPattern) {
                    return riskFound;
                }
            }
        }
//        System.out.println("Risk type: " + riskType + "Risk score: " +riskScore);
        return riskFound;

    }

}
