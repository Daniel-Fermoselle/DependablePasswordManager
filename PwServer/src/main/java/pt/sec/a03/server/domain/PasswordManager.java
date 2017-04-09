package pt.sec.a03.server.domain;

import java.sql.SQLException;

import pt.sec.a03.server.database.Database;
import pt.sec.a03.server.exception.AlreadyExistsException;
import pt.sec.a03.server.exception.DataNotFoundException;
import pt.sec.a03.server.exception.ForbiddenAccessException;
import pt.sec.a03.server.exception.InvalidArgumentException;

public class PasswordManager {

    public void addUser(String publicKey) {
        if (publicKey == null) {
            throw new InvalidArgumentException("The argument " + publicKey + " is not suitable to create a new user");
        }
        try {
            Database db = new Database();
            User user = db.getUserByPK(publicKey);
            if (user != null) {
                throw new AlreadyExistsException(
                        "Already exists in the server user with the following Public Key: " + publicKey);
            } else {
                db.saveUser(publicKey);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public User getUserByID(String id) {
        try {
            Database db = new Database();
            User user = db.getUserByID(id);
            if (user != null) {
                return user;
            } else {
                throw new DataNotFoundException("The user with the ID " + id + " doesn't exist in the server");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public User getUserByPK(String publicKey) {
        try {
            Database db = new Database();
            User user = db.getUserByPK(publicKey);
            if (user != null) {
                return user;
            } else {
                throw new DataNotFoundException(
                        "The user with the Public Key " + publicKey + " doesn't exist in the server");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public void updateUserWithID(String id, String publicKey) {
        try {
            Database db = new Database();
            User user = db.getUserByID(id);
            if (user != null) {
                db.updateUser(id, publicKey);
            } else {
                throw new DataNotFoundException("The user with the ID " + id + " doesn't exist in the server");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public Triplet getTriplet(String username, String domain, String publicKey) {
        try {
            Database db = new Database();
            if (username == null || domain == null) {
                throw new InvalidArgumentException("Username or domain invalid");
            }
            User u = db.getUserByPK(publicKey);
            if (u == null) {
                throw new DataNotFoundException(
                        "The user with the public key " + publicKey + " doesn't exist in the server");
            }
            Triplet t = db.getTriplet(username, domain);
            if (t == null) {
                throw new DataNotFoundException("Username: " + username + " or Domain: " + domain + " not found");
            } else {
                if (t.getUserID() != u.getUserID()) {
                    throw new ForbiddenAccessException("The user with the public key " + publicKey
                            + " has no permissions to access this information");
                }
                return t;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public Triplet saveTriplet(Triplet t, String publicKey) {
        if (publicKey == null || t == null || t.getPassword() == null || t.getUsername() == null
                || t.getDomain() == null) {
            throw new InvalidArgumentException("The arguments provided are not suitable to create a new password");
        }
        try {
            Database db = new Database();
            User u = db.getUserByPK(publicKey);
            Triplet newTriplet = db.getTriplet(t.getUsername(), t.getDomain());//TODO Isto nao devia estar em baixo do primeiro if
            if (u == null) {
                throw new DataNotFoundException(
                        "The user with the public key " + publicKey + " doesn't exist in the server");
            }
            if (newTriplet != null) {
                if (newTriplet.getUserID() == u.getUserID()) {
                    db.updateTriplet(t);
                } else {
                    throw new ForbiddenAccessException("The user with the public key " + publicKey
                            + " has no permissions to access this information");
                }
            } else {
                db.saveTriplet(t, u.getUserID());
            }

            return db.getTriplet(t.getUsername(), t.getDomain());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public String getHash(String username, String domain, String publicKey) {
        return getTriplet(username, domain, publicKey).getHash();
    }

    public void saveHash(long tripletID, String hashPw) {
        Database db = new Database();
        try {
            db.updateHash(tripletID, hashPw);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());

        }
    }

    public String getUserMetaInfo(String publicKey) {
        try {
            if(publicKey == null){
                throw new InvalidArgumentException("PublicKey null is not valid");
            }
            //Get User
            User u = getUserByPK(publicKey);

            //Get Nonce and cipher it
            return u.getNonce() + "";
        } catch (DataNotFoundException e) {
            return 0 + "";
        }
    }

    public String getNewNonceForUser(String publicKey) {
        try {
            User user = getUserByPK(publicKey);

            long nonce = user.getNonce();
            nonce = nonce + 1;
            
            Database database = new Database();
            database.updateUserNonce(user.getUserID() + "", nonce);
            return nonce+"";
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public boolean validateNonceForUer(String publicKey, long nonceToValidate) {
        return Long.parseLong(getUserMetaInfo(publicKey)) == nonceToValidate;
    }
}
