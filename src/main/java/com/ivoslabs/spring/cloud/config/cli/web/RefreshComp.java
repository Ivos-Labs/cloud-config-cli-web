package com.ivoslabs.spring.cloud.config.cli.web;

/**
 * Component to re-set the properties from a spring-cloud-config-server service
 *
 * @since 1.0.0
 * @author imperezivan
 *
 */
public interface RefreshComp {

    /**
     * Reloads the remote properties and restarts singleton beans
     *
     * @return status as a json
     * @author imperezivan
     *
     */
    String refresh();
}