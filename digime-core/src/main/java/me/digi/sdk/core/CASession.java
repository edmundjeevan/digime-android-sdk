package me.digi.sdk.core;

import com.google.gson.annotations.SerializedName;

import junit.framework.Assert;

import java.util.concurrent.ConcurrentMap;

import me.digi.sdk.core.session.CASessionManager;
import me.digi.sdk.core.session.Session;
import me.digi.sdk.core.session.SessionListener;
import me.digi.sdk.core.session.SessionManager;

public class CASession implements Session{

    public static final long DEFAULT_EXPIRY = 60000;

    @SerializedName("sessionKey")
    public String sessionKey;

    @SerializedName("expiry")
    public long expiry;

    final CASessionManager sessionManager;
    volatile long lastAccessed;
    final long creationTime;

    private String sessionId;
    private volatile boolean invalid = false;
    private volatile boolean invalidationStarted = false;

    public CASession(String sessionKey) {
        this(sessionKey, System.currentTimeMillis() + DEFAULT_EXPIRY);
    }

    public CASession(String sessionKey, long expiry) {
        this(sessionKey,expiry, (CASessionManager) DigiMeClient.getInstance().getSessionManager());
    }

    public CASession(String sessionKey, long expiry, CASessionManager sessionManager) {
        if (sessionKey == null) {
            throw new IllegalArgumentException("Valid session key must be provided.");
        }
        this.sessionKey = sessionKey;
        this.expiry = expiry;
        this.sessionManager = sessionManager;
        creationTime = lastAccessed = System.currentTimeMillis();
        if (sessionManager != null) {
            sessionManager.dispatch.sessionCreated(this);
        }
    }

    public String getSessionKey() { return sessionKey; }
    public long getExpiry() { return expiry; }


    @Override
    public String getId() {
        return sessionId;
    }

    @Override
    public boolean isValid() {
        return System.currentTimeMillis() >= expiry && !invalid;
    }

    @Override
    public void requestCompleted() {
        if (!invalid) {
            lastAccessed = System.currentTimeMillis();
        }
    }

    @Override
    public long getCreatedTime() {
        return creationTime;
    }

    @Override
    public long getLastAccessedTime() {
        return lastAccessed;
    }

    @Override
    public void invalidate() {
        synchronized(CASession.this) {
            CASession sess = sessionManager.invalidateSession(sessionId);
            if (sess == null) {
                throw new IllegalStateException("Session already invalidated");
            }
            invalidationStarted = true;
        }

        sessionManager.dispatch.sessionDestroyed(this, SessionListener.DestroyedReason.INVALIDATED);
        invalid = true;
    }

    @Override
    public SessionManager getSessionManager() {
        return sessionManager;
    }

    @Override
    public String changeSessionId(String id) {
        final String oldId = sessionId;
        this.sessionId = id;
        if(!invalid) {
            sessionManager.setSession(id, this);
        }
        sessionManager.invalidateSession(oldId);
        return id;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || obj.getClass() != getClass()) return false;
        final CASession session = (CASession) obj;
        return sessionKey != null ? sessionKey.equals(session.sessionKey) : session.sessionKey == null;
    }

    @Override
    public int hashCode() {
        return sessionKey != null ? sessionKey.hashCode() : 0;
    }
}
