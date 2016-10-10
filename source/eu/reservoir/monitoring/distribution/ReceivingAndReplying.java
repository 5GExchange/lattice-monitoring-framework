/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.reservoir.monitoring.distribution;

import eu.reservoir.monitoring.core.plane.ControlPlaneReplyMessage;
import java.io.IOException;

/**
 *
 * @author uceeftu
 */
public interface ReceivingAndReplying extends Receiving {
    public int transmitReply(ControlPlaneReplyMessage answer, MetaData metadata) throws IOException;
}
