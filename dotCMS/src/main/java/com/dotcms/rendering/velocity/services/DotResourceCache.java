/**
 * 
 */
package com.dotcms.rendering.velocity.services;

import com.dotcms.repackage.com.google.common.collect.ImmutableSet;

import com.dotmarketing.business.Cachable;
import com.dotmarketing.business.CacheLocator;
import com.dotmarketing.business.DotCacheAdministrator;
import com.dotmarketing.business.DotCacheException;
import com.dotmarketing.util.Logger;

import java.util.HashSet;
import java.util.Set;

import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.ResourceCache;

/**
 * @author Jason Tesser
 * @author Andres Olarte
 * @since 1.6.5 The DotResourceCache was created to allow velocities cache to be distributed across
 *        nodes in a cluster. It also allows the dotCMS to set velocity to always cache and pull
 *        from cache Our services methods which generate the velocity files will handle the filling
 *        and removing of The cache. If something is not in cache though the DotResourceLoader will
 *        be called.
 */
public class DotResourceCache implements ResourceCache, Cachable {


    private DotCacheAdministrator cache;

    private String primaryGroup = "VelocityCache";
    private String macroCacheGroup = "VelocityMacroCache";
    // region's name for the cache
    private String[] groupNames = {primaryGroup, macroCacheGroup};
    private static final String MACRO_PREFIX = "MACRO_PREFIX";
    private final Set<String> ignoreGlobalVM;



    public DotResourceCache() {
        cache = CacheLocator.getCacheAdministrator();
        String files = System.getProperty(RuntimeConstants.VM_LIBRARY);
        Set<String> holder = new HashSet<>();
        for (String file : files.split(",")) {
            if (file != null) {
                // System.out.println("FILE: " +file);
                holder.add(file.trim());
            }
        }
        ignoreGlobalVM = ImmutableSet.copyOf(holder);
    }

    public String[] getMacro(String name) {


        String[] rw = null;
        try {
            rw = (String[]) cache.get(MACRO_PREFIX + name, macroCacheGroup);
        } catch (DotCacheException e) {
            Logger.debug(this, "Cache Entry not found", e);
        }
        return rw;

    }

    public void putMacro(String name, String content) {
        if (name == null || content == null) {
            Logger.warn(this.getClass(), "Cannot add a null macro to cache:" + name + " / " + content);
            return;
        }
        String[] rw = {name, content};
        cache.put(MACRO_PREFIX + name, rw, macroCacheGroup);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.velocity.runtime.resource.ResourceCache#get(java.lang.Object)
     */
    @Override
    public Resource get(final Object resourceKey) {

        final VelocityResourceKey key = new VelocityResourceKey(resourceKey);

        try {
            return (Resource) cache.get(key.cacheKey, primaryGroup);
        } catch (DotCacheException e) {
            Logger.debug(this, "Cache Entry not found", e);
        }
    
        return null;
    }

    @Override
    public void initialize(RuntimeServices rs) {
        cache = CacheLocator.getCacheAdministrator();
    }

    public void addMiss(Object resourceKey) {
        final VelocityResourceKey key = new VelocityResourceKey(resourceKey);
        Logger.info(this.getClass(), "velocityMiss:" + key);
    }

    public boolean isMiss(Object resourceKey) {
        return false;
    }

    @Override
    public Resource put(final Object resourceKey, final Resource resource) {
        final VelocityResourceKey key = new VelocityResourceKey(resourceKey);
        if (resource != null && ignoreGlobalVM.contains(resource.getName())) {
            return resource;
        }


        // Add the key to the cache
        cache.put(key.cacheKey, resource, primaryGroup);

        return resource;

    }

    @Override
    public Resource remove(final Object resourceKey) {

        final VelocityResourceKey key = new VelocityResourceKey(resourceKey);

        try {
            cache.remove(key.cacheKey, primaryGroup);
        } catch (Exception e) {
            Logger.debug(this, e.getMessage(), e);
        }
        return null;
    }

    public void clearCache() {
        for (String group : groupNames) {
            cache.flushGroup(group);
        }

    }



    public String[] getGroups() {
        return groupNames;
    }

    public String getPrimaryGroup() {
        return primaryGroup;
    }



}
