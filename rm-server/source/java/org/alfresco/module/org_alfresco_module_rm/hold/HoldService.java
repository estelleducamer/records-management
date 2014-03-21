/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.module.org_alfresco_module_rm.hold;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Hold service interface.
 *
 * @author Tuna Aksoy
 * @since 2.2
 */
public interface HoldService
{
    /**
     * Indicates whether the passed node reference is a hold.  A hold is a container for a group of frozen object and contains the freeze
     * reason.
     *
     * @param nodeRef   hold node reference
     * @return boolean  true if hold, false otherwise
     */
    boolean isHold(NodeRef nodeRef);
    
    /**
     * Gets the list of all the holds within the holds container in the given file plan
     *
     * @param filePlan The {@link NodeRef} of the file plan
     * @return List of hold node references
     */
    List<NodeRef> getHolds(NodeRef filePlan);

    /**
     * 
     * @param name
     * @return
     */
    NodeRef getHold(NodeRef filePlan, String name);

    /**
     * Gets the list of all the holds within the holds container for the given node reference
     *
     * @param nodeRef The {@link NodeRef} of the record / record folder
     * @param includedInHold <code>true</code> to retrieve the list of hold node references which will include the node reference
     * <code>false</code> to get a list of node references which will not have the given node reference
     * @return List of hold node references
     */
    List<NodeRef> heldBy(NodeRef nodeRef, boolean includedInHold);
    
    /**
     * 
     * @param ndoeRef
     * @return
     */
    List<NodeRef> getHeld(NodeRef hold);
    
    /**
     * 
     * @param filePlan
     * @param name
     * @param reason
     * @param description
     * @return
     */
    NodeRef createHold(NodeRef filePlan, String name, String reason, String description);
    
    /**
     * 
     * @param hold
     * @return
     */
    String getHoldReason(NodeRef hold);
    
    /**
     * 
     * @param hold
     * @param reason
     */
    void setHoldReason(NodeRef hold, String reason);
    
    /**
     * 
     * @param hold
     */
    void deleteHold(NodeRef hold);
    
    /**
     * Adds the record to the given hold
     *
     * @param hold The {@link NodeRef} of the hold
     * @param nodeRef The {@link NodeRef} of the record / record folder which will be added to the given hold
     */
    void addToHold(NodeRef hold, NodeRef nodeRef);
    
    /**
     * 
     * @param hold
     * @param nodeRefs
     */
    void addToHold(NodeRef hold, List<NodeRef> nodeRefs);

    /**
     * Adds the record to the given list of holds
     *
     * @param holds The list of {@link NodeRef}s of the holds
     * @param nodeRef The {@link NodeRef} of the record / record folder which will be added to the given holds
     */
    void addToHolds(List<NodeRef> holds, NodeRef nodeRef);

    /**
     * Removes the record from the given hold
     *
     * @param hold The {@link NodeRef} of the hold
     * @param nodeRef The {@link NodeRef} of the record / record folder which will be removed from the given hold
     */
    void removeFromHold(NodeRef hold, NodeRef nodeRef);
    
    /**
     * 
     * @param hold
     * @param nodeRefs
     */
    void removeFromHold(NodeRef hold, List<NodeRef> nodeRefs);

    /**
     * Removes the record from the given list of hold
     *
     * @param holds The list {@link NodeRef}s of the holds
     * @param nodeRef The {@link NodeRef} of the record / record folder which will be removed from the given holds
     */
    void removeFromHolds(List<NodeRef> holds, NodeRef nodeRef);
    
    /**
     * 
     * @param nodeRef
     */
    void removeFromAllHolds(NodeRef nodeRef);
    
    /**
     * 
     * @param nodeRefs
     */
    void removeFromAllHolds(List<NodeRef> nodeRefs);
}