package com.chat.base.endpoint;

import com.chat.base.utils.WKReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 2020-09-01 18:17
 */
public class EndpointManager {
    private EndpointManager() {
    }

    private static class EndpointManagerBinder {
        final static EndpointManager manager = new EndpointManager();
    }

    public static EndpointManager getInstance() {
        return EndpointManagerBinder.manager;
    }

    private ConcurrentHashMap<String, List<Endpoint>> endpointList;

    private void register(String sid, String category, int sort, EndpointHandler iHandler) {
        if (endpointList == null) endpointList = new ConcurrentHashMap<>();
        List<Endpoint> Endpoints;
        if (endpointList.containsKey(category)) {
            Endpoints = endpointList.get(category);
            if (Endpoints == null) Endpoints = new ArrayList<>();
        } else {
            Endpoints = new ArrayList<>();
        }
        Endpoints.add(new Endpoint(sid, category, sort, iHandler));
        endpointList.put(category, Endpoints);
    }

    public void setMethod(String sid, EndpointHandler EndpointHandler) {
        register(sid, "", 0, EndpointHandler);
    }

    public void setMethod(String sid, String category, EndpointHandler EndpointHandler) {
        register(sid, category, 0, EndpointHandler);
    }

    public void setMethod(String sid, String category, int sort, EndpointHandler EndpointHandler) {
        register(sid, category, sort, EndpointHandler);
    }

    public void remove(String sid) {
        for (String category : endpointList.keySet()) {
            List<Endpoint> list = endpointList.get(category);
            if (WKReader.isNotEmpty(list)) {
                int max = list.size() - 1;
                for (int i = max; i >= 0; i--) {
                    if (list.get(i).sid.equals(sid)) {
                        list.remove(i);
                        break;
                    }
                }
                if (WKReader.isEmpty(list)) {
                    endpointList.remove(category);
                } else {
                    endpointList.put(category, list);
                }
            }
        }
    }

    public Object invoke(String sid, Object param) {
        Endpoint Endpoint = null;
        for (String category : endpointList.keySet()) {
            List<Endpoint> list = endpointList.get(category);
            if (WKReader.isNotEmpty(list)) {
                int max = list.size() - 1;
                for (int i = max; i >= 0; i--) {
                    if (list.get(i).sid.equals(sid)) {
                        Endpoint = list.get(i);
                        break;
                    }
                }
            }
        }
        if (Endpoint != null && Endpoint.iHandler != null) {
            return Endpoint.iHandler.invoke(param);
        }
        return Endpoint;
    }

    public <K> List<K> invokes(String category, Object object) {
        if (endpointList == null || endpointList.isEmpty() || !endpointList.containsKey(category))
            return null;
        else {
            List<K> list = new ArrayList<>();
            List<Endpoint> tempList = endpointList.get(category);
            if (tempList == null || tempList.isEmpty()) {
                return list;
            } else {
                Collections.sort(tempList);
                for (int i = 0; i < tempList.size(); i++) {
                    K result = (K) tempList.get(i).iHandler.invoke(object);
                    if (result != null)
                        list.add(result);
                }
            }
            return list;
        }
    }

    /**
     * 清理指定分类的所有端点
     * @param category 分类名称
     */
    public void clearCategory(String category) {
        if (endpointList != null && endpointList.containsKey(category)) {
            endpointList.remove(category);
        }
    }

    /**
     * 清理所有端点
     */
    public void clearAll() {
        if (endpointList != null) {
            endpointList.clear();
        }
    }

    /**
     * 检查指定分类是否已存在端点
     * @param category 分类名称
     * @return 是否存在
     */
    public boolean hasCategory(String category) {
        return endpointList != null && endpointList.containsKey(category) && 
               endpointList.get(category) != null && !endpointList.get(category).isEmpty();
    }
}
