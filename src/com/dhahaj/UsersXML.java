package com.dhahaj;

//import static com.tester.DebugLogger.debug;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.simpleframework.xml.*;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.OutputNode;
import static processing.core.PApplet.println;

@Root
public class UsersXML {

    @ElementList
    private static List<User> users;

    public UsersXML() {
        super();
        users = new ArrayList<User>();
    }

    public void addUser(User user) {
        users.add(user);
    }

    public void removeUser(String name) {
        for (User u : users) {
            if (u.name.equalsIgnoreCase(name)) {
                users.remove(u);
                break;
            }
        }
    }

    public User getUser(String name) {
        User foundUser = null;
        for (User u : users) {
            if (u.name.equalsIgnoreCase(name)) {
                foundUser = u;
                break;
            }
        }
        return foundUser;
    }

    public static User findUser(String name) {
        for (User u : users) {
            if (u.name.equals(name)) {
                return u;
            }
        }
        return null;
    }

    public List<String> getUserNames() {
        ArrayList<String> nameList = new ArrayList<String>();
        for (User u : users) {
            nameList.add(u.name);
        }
        return nameList;
    }

    public List<User> getList() {
        return users;
    }

    /**
     Sets the {@link User} {@link List}.

     @param newList The new {@link List}.
     */
    public void setUserList(java.util.List newList) {
        this.users = newList;
    }

    /**

     */
    public static class User {

        @Attribute
        private String name;

        @Attribute
        private byte[] password;

        @Attribute
        private boolean admin;

        public User() {
            super();
        }

        public User(String n, String p, boolean a) {
            this.name = n;
            this.password = p.getBytes();
            this.admin = a;
        }

        public void setPassword(String p) {
            this.password = p.getBytes();
        }

        public String getPassword() {
            return new String(password);
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setAdmin(boolean a) {
            admin = a;
        }

        public String getName() {
            return name;
        }

        public boolean isAdmin() {
            return admin;
        }

        @Override
        public String toString() {
            return String.format("User=%s, Pass=%s, Administrator=%b", name, getPassword(), admin);
        }

        @Override
        public int hashCode() {
            //f:off
            return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
                    append(name).
                    append(password).
                    append(admin).
                    toHashCode();
            //f:on
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            User rhs = (User) obj;
            //f:off
            return new EqualsBuilder()
                    .append(name, rhs.name)
                    .append(password, rhs.password)
                    .append(admin, rhs.admin)
                    .isEquals();
            //f:on
        }
    }

    public static boolean checkPassword(String username, String password) {
        User thisUser = findUser(username);
        if (thisUser == null) {
            return false;
        }
        return thisUser.getPassword().equalsIgnoreCase(password);
    }

    public boolean checkPassword(User user, String pass) {
        return checkPassword(user.name, pass);
    }

    public int size() {
        return users.size();
    }

    public String[] toStringArray() {
        return getUserNames().toArray(new String[size()]);
    }

    public static UsersXML read(File file) {
        try {
            return (UsersXML) loadXml(UsersXML.class, file);
        } catch (Exception exception) {
            throw new RuntimeException("Error loading users xml file", exception);
        }
    }

    public static void saveXML(File f, UsersXML content) {
        Persister persister = new Persister();
        try {
            persister.write(content, f);
        } catch (Exception ex) {
            // debug(ex);
            println(ex);
            Logger.getLogger(UsersXML.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     Reads a serializable object from an XML file.

     @param type   The Class type of the object.
     @param source The File to read.
     @return An object of the provided Class type.
     @throws Exception #see Exception
     */
    public static Object loadXml(Class<?> type, File source) throws Exception {
        return new Persister().read(type, source);
    }
}

//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.simpleframework.xml.*;
//
//@Root
//public class UsersXML {
//
//  @ElementList
//  private List<User> users = new ArrayList<>();
//
//  public UsersXML() {
//    super();
////    users = new ArrayList<User>();
//  }
//
//  public void addUser(User user) {
//    users.add(user);
//  }
//
//  public User getUser(String name) {
//    User foundUser = null;
//    for (User u : users) {
//      if (u.name.equalsIgnoreCase(name)) {
//        foundUser = u;
//        break;
//      }
//    }
//    return foundUser;
//  }
//
//  public List<String> getUserNames() {
//    ArrayList<String> nameList = new ArrayList<String>();
//    for (User u : users) {
//      nameList.add(u.name);
//    }
//    return nameList;
//  }
//
//  public List<User> getList() {
//    return users;
//  }
//
//  public static class User {
//
//    @Attribute
//    private String name;
//
//    @Attribute
//    private String password;
//
//    @Attribute
//    private boolean admin = true;
//
//    public User() {
//      super();
//    }
//
//    public User(String n, String p, boolean a) {
//      this.name = n;
//      this.password = p;
//      this.admin = a;
//    }
//
//    public String getPassword() {
//      return password;
//    }
//
//    public String getName() {
//      return name;
//    }
//
//    public boolean isAdmin() {
//      return admin;
//    }
//
//    @Override
//    public String toString() {
//      return String.format("User=%s\tPass=%s\tAdministrator=%b", name, getPassword(), admin);
//    }
//
//  }
//
//  public boolean checkPassword(String username, String password) {
//    User thisUser = getUser(username);
//    if (thisUser == null) {
//      return false;
//    }
//    return thisUser.getPassword().equalsIgnoreCase(password);
//  }
//
//  public boolean checkPassword(User user, String pass) {
//    return checkPassword(user.name, pass);
//  }
//
//  public int size() {
//    return users.size();
//  }
//
//  public String[] toStringArray() {
//    return getUserNames().toArray(new String[size()]);
//  }
//
//}
