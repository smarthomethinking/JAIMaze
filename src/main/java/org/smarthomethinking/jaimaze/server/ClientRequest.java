package org.smarthomethinking.jaimaze.server;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 *
 * @author gde
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ClientRequest {

    private Config config = new Config();
    private History history;
    private Position currentPosition;

}
