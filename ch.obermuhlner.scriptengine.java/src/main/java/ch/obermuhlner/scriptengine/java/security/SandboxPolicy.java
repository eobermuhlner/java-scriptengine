package ch.obermuhlner.scriptengine.java.security;

import ch.obermuhlner.scriptengine.java.MemoryClassLoader;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.*;
import java.util.Scanner;
import java.util.regex.Pattern;

public class SandboxPolicy extends Policy {

    private final Policy defaultPolicy;
    private final PermissionCollection sandboxPermissions;

    public SandboxPolicy(Policy defaultPolicy, File policyFile) {
        this.defaultPolicy = defaultPolicy;
        sandboxPermissions = loadPolicyFile(policyFile);
    }

    @Override
    public PermissionCollection getPermissions(ProtectionDomain domain) {
        if (isSandbox(domain)) {
            return getSandboxPermissions();
        }

        return getDefaultPermissions(domain);
    }

    private PermissionCollection getDefaultPermissions(ProtectionDomain domain) {
        if (defaultPolicy != null) {
            return defaultPolicy.getPermissions(domain);
        }

        Permissions permissions = new Permissions();
        permissions.add(new AllPermission());
        return permissions;
    }

    private PermissionCollection getSandboxPermissions() {
        return sandboxPermissions;
    }

    private PermissionCollection loadPolicyFile(File policyFile) {
        Permissions permissions = new Permissions();

        ClassLoader classLoader = getClass().getClassLoader();

        try (BufferedReader reader = new BufferedReader(new FileReader(policyFile))) {
            String line = reader.readLine();
            while (line != null) {
                String[] statements = line.split(Pattern.quote(";"));
                for (String statement : statements) {
                    Scanner scanner = new Scanner(statement);

                    if (scanner.hasNext(Pattern.quote("permission"))) {
                        scanner.next(Pattern.quote("permission"));
                        String type = scanner.next("[A-Za-z0-9$_.]*");
                        Class<?> permissionClass = classLoader.loadClass(type);

                        String targetName = unquote(scanner.next("\"[^\"]*\""));
                        Object permissionInstance;
                        if (scanner.hasNext()) {
                            String action = unquote(scanner.next("\".*\""));
                            Constructor<?> constructor = permissionClass.getConstructor(String.class, String.class);
                            permissionInstance = constructor.newInstance(targetName, action);
                        } else {
                            Constructor<?> constructor = permissionClass.getConstructor(String.class);
                            permissionInstance = constructor.newInstance(targetName);
                        }

                        permissions.add((Permission) permissionInstance);
                    }
                }
                line = reader.readLine();
            }
        } catch (FileNotFoundException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return permissions;
    }

    private String unquote(String string) {
        if (string.startsWith("\"") && string.endsWith("\"")) {
            return string.substring(1, string.length() - 1);
        }
        return string;
    }

    private boolean isSandbox(ProtectionDomain domain) {
        return domain.getClassLoader() instanceof MemoryClassLoader;
    }
}
