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

    //----------------------------------------
    //			User Functions
    //----------------------------------------

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

            this.db.updateNonce(user.getPublicKey(), nonce);
            return nonce+"";
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public boolean validateNonceForUer(String publicKey, long nonceToValidate) {
        return Long.parseLong(getUserMetaInfo(publicKey)) == nonceToValidate;
    }

    //----------------------------------------
    //			Bonrr Functions
    //----------------------------------------

    public Bonrr getBonrrInstance(String bonrr, String domain, String username) {
        try {
            getUserByPK(bonrr);
            Bonrr bonrrInstance = this.db.getBonrrInstance(bonrr, domain, username);
            if (bonrrInstance != null) {
                return bonrrInstance;
            } else {
                return new Bonrr(bonrr, 0, 0, domain, username);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public Triplet getBonrr(String bonrr, String username, String domain) {
        if (bonrr == null || username == null || domain == null) {
            throw new InvalidArgumentException("The arguments provided are not suitable to create a new password");
        }
	    try {
            getUserByPK(bonrr);
            Triplet bonrrInfo = this.db.getBonrr(bonrr, username, domain);
            if (bonrrInfo != null) {
                return bonrrInfo;
            } else {
            	throw new DataNotFoundException("Bonrr not found");
            }
        } catch (SQLException e) {
	        e.printStackTrace();
	        throw new RuntimeException(e.getMessage());
        }
    }
    
    public void saveBonrr(String bonrr, Triplet t) {
        if (bonrr == null || t == null || t.getPassword() == null || t.getUsername() == null
                || t.getDomain() == null || t.getHash() == null || t.getSignature() == null) {
            throw new InvalidArgumentException("The arguments provided are not suitable to create a new password");
        }
        try {
            getUserByPK(bonrr);
            if(!this.db.checkUserAndDomain(bonrr,t)){ 
            	throw new ForbiddenAccessException("The user with the public key " + bonrr
                        + " has no permissions to access this information");
            }
            try {
                Triplet bonrrInfo = getBonrr(bonrr, t.getUsername(), t.getDomain());
                if (hasRank(bonrrInfo, t)) {
                    this.db.updateBonrr(bonrr, t);
                }
                else{
                    this.db.saveBonrr(bonrr, t);
                }
            } catch (DataNotFoundException e){
                this.db.saveBonrr(bonrr, t);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

    }

    private boolean hasRank(Triplet bonrrInfo, Triplet t) {
	    if(bonrrInfo.getWts() == t.getWts() && bonrrInfo.getRank() < t.getRank()){
	        return true;
        }
        return false;
    }


}
