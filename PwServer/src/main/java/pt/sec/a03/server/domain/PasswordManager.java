package pt.sec.a03.server.domain;

import java.sql.SQLException;

import pt.sec.a03.common_classes.Bonrr;
import pt.sec.a03.server.database.Database;
import pt.sec.a03.server.exception.AlreadyExistsException;
import pt.sec.a03.server.exception.DataNotFoundException;
import pt.sec.a03.server.exception.ForbiddenAccessException;
import pt.sec.a03.server.exception.InvalidArgumentException;

public class PasswordManager {
	
	private Database db;
	
	public PasswordManager(){
		this.db = new Database();
	}

    public void addUser(String publicKey) {
        if (publicKey == null) {
            throw new InvalidArgumentException("The argument " + publicKey + " is not suitable to create a new user");
        }
        try {
            User user = this.db.getUserByPK(publicKey);
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

    public User getUserByPK(String publicKey) {
        try {
            User user = this.db.getUserByPK(publicKey);
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

    public Triplet getTriplet(String username, String domain, String publicKey) {
        try {
            if (username == null || domain == null) {
                throw new InvalidArgumentException("Username or domain invalid");
            }
            User u = this.db.getUserByPK(publicKey);
            if (u == null) {
                throw new DataNotFoundException(
                        "The user with the public key " + publicKey + " doesn't exist in the server");
            }
            Triplet t = this.db.getTriplet(username, domain);
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
                || t.getDomain() == null || t.getHash() == null) {
            throw new InvalidArgumentException("The arguments provided are not suitable to create a new password");
        }
        try {
            User u = this.db.getUserByPK(publicKey);
            if (u == null) {
                throw new DataNotFoundException(
                        "The user with the public key " + publicKey + " doesn't exist in the server");
            }
            Triplet newTriplet = this.db.getTriplet(t.getUsername(), t.getDomain());
            if (newTriplet != null) {
                if (newTriplet.getUserID() == u.getUserID()) {
                    this.db.updateTriplet(t);
                } else {
                    throw new ForbiddenAccessException("The user with the public key " + publicKey
                            + " has no permissions to access this information");
                }
            } else {
                this.db.saveTriplet(t, u.getUserID());
            }

            return this.db.getTriplet(t.getUsername(), t.getDomain());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public String getHash(String username, String domain, String publicKey) {
        return getTriplet(username, domain, publicKey).getHash();
    }

    public void saveHash(long tripletID, String hashPw) {
        try {
            this.db.updateHash(tripletID, hashPw);
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
            
            this.db.updateUserNonce(user.getUserID() + "", nonce);
            return nonce+"";
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public boolean validateNonceForUer(String publicKey, long nonceToValidate) {
        return Long.parseLong(getUserMetaInfo(publicKey)) == nonceToValidate;
    }

    public Bonrr getBonrr(String bonrr) {
	    try {
            Bonrr bonrrInstance = this.db.getBonrr(bonrr);
            if (bonrrInstance != null) {
                return bonrrInstance;
            } else {
                return new Bonrr(bonrr, 0);
            }
        } catch (SQLException e) {
	        e.printStackTrace();
	        throw new RuntimeException(e.getMessage());
        }
    }

    public void saveBonrr(String bonrr, String wts, String signature, Triplet t) {
	    try {
            this.db.saveBonrr(bonrr, wts, signature, t);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

    }

    public String getNewBonrrID(String publicKey) {
        try {
            User user = getUserByPK(publicKey);

            long bonrrID = user.getBonrrID();
            bonrrID = bonrrID + 1;

            this.db.updateUserBonrrID(user.getUserID() + "", bonrrID);
            return (user.getUserID() + "") + (bonrrID + "");
        } catch (DataNotFoundException e) {
            return 0 + "";
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
}
