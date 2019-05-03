/*
 * Copyright 2010-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package schedulerbackend;

import com.sabanciuniv.smartschedule.app.Task;
import com.sabanciuniv.smartschedule.app.ViewSchedule;

import java.util.ArrayList;

@com.amazonaws.mobileconnectors.apigateway.annotation.Service(endpoint = "https://ehhupl1lh5.execute-api.eu-central-1.amazonaws.com/backend")
public interface SchedulerbackendClient {


    /**
     * A generic invoker to invoke any API Gateway endpoint.
     * @param request
     * @return ApiResponse
     */
    com.amazonaws.mobileconnectors.apigateway.ApiResponse execute(com.amazonaws.mobileconnectors.apigateway.ApiRequest request);

    @com.amazonaws.mobileconnectors.apigateway.annotation.Operation(path = "", method = "GET")
    ArrayList<Task> sortTask(ArrayList<Task> st, ViewSchedule.distanceMatrix dm);

    /**
     * 
     * 
     * @return void
     */
    @com.amazonaws.mobileconnectors.apigateway.annotation.Operation(path = "/items", method = "OPTIONS")
    void itemsOptions();
    
    /**
     * 
     * 
     * @return void
     */
    @com.amazonaws.mobileconnectors.apigateway.annotation.Operation(path = "/items/{proxy+}", method = "OPTIONS")
    void itemsProxyOptions();

}

