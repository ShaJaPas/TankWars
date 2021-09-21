package com.tank.wars.tools.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.tank.wars.tools.encryption.SecureData;

public class PreferenceManager {
    private Preferences loginPref;
    private Preferences settingsPref;
    private SecureData data;

    public PreferenceManager(SecureData data) {
        this.data = data;
        loginPref = Gdx.app.getPreferences("Login");
        settingsPref = Gdx.app.getPreferences("Settings");
    }

    public void saveLogin(String email, String password) throws Exception{
        loginPref.putString("email", Base64.encode(data.makeDataSecureDefault(data.serialize(email))));
        loginPref.putString("password", Base64.encode(data.makeDataSecureDefault(data.serialize(password))));
    }

    public String[] getLogin() throws Exception {
        String[] res = new String[2];
        res[0] = loginPref.contains("email") ?  data.deserialize(data.makeDataUnSecureDefault(Base64.decode(loginPref.getString("email"))), String.class) : "";
        res[1] = loginPref.contains("password") ?  data.deserialize(data.makeDataUnSecureDefault(Base64.decode(loginPref.getString("password"))), String.class) : "";
        return res;
    }

    public void saveRememberMe(boolean state){
        settingsPref.putBoolean("RememberMe", state);
    }

    public boolean getRememberMe(){
        return settingsPref.contains("RememberMe") && settingsPref.getBoolean("RememberMe");
    }
    public void save(){
        loginPref.flush();
        settingsPref.flush();
    }
}
