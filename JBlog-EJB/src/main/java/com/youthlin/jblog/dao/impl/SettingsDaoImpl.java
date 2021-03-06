package com.youthlin.jblog.dao.impl;

import com.youthlin.jblog.model.Settings;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lin on 2016-09-08-008.
 * 设置
 */
@Stateless
public class SettingsDaoImpl extends BaseDaoImpl<Settings, Long> implements com.youthlin.jblog.dao.SettingsDao {
    @Override
    public String get(String name) {
        Settings s = findByName(name);
        if (s != null) {
            return s.getValue();
        }
        return null;
    }

    private Settings findByName(String name) {
        TypedQuery<Settings> query = em.createQuery("select s from Settings as s where s.name=:name", Settings.class);
        query.setParameter("name", name);
        return getSingleResult(query);
    }

    @Override
    public void add(String name, String value) {
        Settings s = findByName(name);
        if (s != null) {
            s.setValue(value);
        } else {
            s = new Settings();
            s.setName(name);
            s.setValue(value);
            em.merge(s);
        }
    }

    @Override
    public Map<String, String> findAll() {
        Map<String, String> map = new HashMap<>();
        List<Settings> list = findAll(Settings.class);
        for (Settings settings : list) {
            map.put(settings.getName(), settings.getValue());
        }
        return map;
    }
}
