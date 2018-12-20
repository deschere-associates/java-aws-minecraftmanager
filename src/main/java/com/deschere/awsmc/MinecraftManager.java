/*
 * Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
package com.deschere.awsmc;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DryRunResult;
import com.amazonaws.services.ec2.model.DryRunSupportedRequest;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Tag;
import java.util.List;
import java.util.ArrayList;

/**
 * Starts or stops and EC2 instance
 * new AWSStaticCredentialsProvider(awsCreds)
 */
public class MinecraftManager
{
	
	private static BasicAWSCredentials awsCreds;
	private static String awsRegion = "us-east-1";
	
	public void setCreds(BasicAWSCredentials newCreds)
	{
		awsCreds = newCreds;
	}
	
	public void setCreds(String accessKeyID, String secretAccessKey) 
	{
		awsCreds = new BasicAWSCredentials(accessKeyID, secretAccessKey);
	}

	public void setRegion(String newRegion)
	{
		awsRegion = newRegion;
	}
	
	public static String getInstanceIDByMCWorldTag(String worldName) 
	{
		String instanceID = "Not Found";
		AmazonEC2 client = ec2Builder();

        List<String> taggers = new ArrayList<String>();
    	taggers.add(worldName);
    	
        Filter filClient = new Filter("tag:MCWorld",taggers);
        
        DescribeInstancesRequest request = new DescribeInstancesRequest().withFilters(filClient);
        DescribeInstancesResult response = client.describeInstances(request);

        instanceID = response.getReservations().get(0).getInstances().get(0).getInstanceId();
    	return instanceID;
	}

	
	public static List<String> getInstancesMCWorldTag() 
	{
		AmazonEC2 client = ec2Builder();
		List<String> worldTags = new ArrayList<String>();
		boolean done = false;

		DescribeInstancesRequest request = new DescribeInstancesRequest();
		while(!done) {
		    DescribeInstancesResult response = client.describeInstances(request);

		    for(Reservation reservation : response.getReservations()) {
		        for(Instance instance : reservation.getInstances()) {
		        	for(Tag tag : instance.getTags()) {
		        		if (tag.getKey().equals("MCWorld")) {
		        			worldTags.add(tag.getValue());
		        		}
		        	}
		        }
		    }

		    request.setNextToken(response.getNextToken());

		    if(response.getNextToken() == null) {
		        done = true;
		    }
		}
		
		return worldTags;
	}
	
	
	public static void descInstances() 
	{
		AmazonEC2 client = ec2Builder();
		
		boolean done = false;

		DescribeInstancesRequest request = new DescribeInstancesRequest();
		while(!done) {
		    DescribeInstancesResult response = client.describeInstances(request);

		    for(Reservation reservation : response.getReservations()) {
		        for(Instance instance : reservation.getInstances()) {
		            System.out.printf(
		                "Found instance with id %s, " +
		                "AMI %s, " +
		                "type %s, " +
		                "state %s " +
		                "and monitoring state %s\n",
		                instance.getInstanceId(),
		                instance.getImageId(),
		                instance.getInstanceType(),
		                instance.getState().getName(),
		                instance.getMonitoring().getState());
		            System.out.printf("Tags: %s\n", instance.getTags().toString());
		        }
		    }

		    request.setNextToken(response.getNextToken());

		    if(response.getNextToken() == null) {
		        done = true;
		    }
		}
	}
	
    public static void startInstance(String instance_id)
    {
        AmazonEC2 ec2 = ec2Builder();

        DryRunSupportedRequest<StartInstancesRequest> dry_request =
            () -> {
            StartInstancesRequest request = new StartInstancesRequest()
                .withInstanceIds(instance_id);

            return request.getDryRunRequest();
        };

        DryRunResult dry_response = ec2.dryRun(dry_request);

        if(!dry_response.isSuccessful()) {
            System.out.printf(
                "Failed dry run to start instance %s", instance_id);

            throw dry_response.getDryRunResponse();
        }

        StartInstancesRequest request = new StartInstancesRequest()
            .withInstanceIds(instance_id);

        ec2.startInstances(request);

        System.out.printf("Successfully started instance %s", instance_id);
    }

    public static void stopInstance(String instance_id)
    {
    	AmazonEC2 ec2 = ec2Builder();

        DryRunSupportedRequest<StopInstancesRequest> dry_request =
            () -> {
            StopInstancesRequest request = new StopInstancesRequest()
                .withInstanceIds(instance_id);

            return request.getDryRunRequest();
        };

        DryRunResult dry_response = ec2.dryRun(dry_request);

        if(!dry_response.isSuccessful()) {
            System.out.printf(
                "Failed dry run to stop instance %s", instance_id);
            throw dry_response.getDryRunResponse();
        }

        StopInstancesRequest request = new StopInstancesRequest()
            .withInstanceIds(instance_id);

        ec2.stopInstances(request);

        System.out.printf("Successfully stop instance %s", instance_id);
    }
    
    public static String getInstanceStateByMCWorldTag(String tag) {
    	String serverStatus = "Unknown";
    	
    	String instID = getInstanceIDByMCWorldTag(tag);
    	AmazonEC2 client = ec2Builder();
    	
    	DescribeInstancesRequest request = new DescribeInstancesRequest();
    	DescribeInstancesResult response = client.describeInstances(request);
    	
    	serverStatus = response.getReservations().get(0).getInstances().get(0).getState().getName();
    	
    	return serverStatus;
    }

    public static String getInstanceIPByMCWorldTag(String tag) {
    	String serverIP = "Unknown";
    	
    	String instID = getInstanceIDByMCWorldTag(tag);
    	AmazonEC2 client = ec2Builder();
    	
    	DescribeInstancesRequest request = new DescribeInstancesRequest();
    	DescribeInstancesResult response = client.describeInstances(request);
    	
    	serverIP = response.getReservations().get(0).getInstances().get(0).getPublicIpAddress();
    	
    	return serverIP;
    }
    
    private static final AmazonEC2 ec2Builder() {
    
		AmazonEC2ClientBuilder cbec2 = AmazonEC2ClientBuilder.standard();
		cbec2.setCredentials(new AWSStaticCredentialsProvider(awsCreds));
		cbec2.setRegion(awsRegion);
		
	    final AmazonEC2 ec2 = cbec2.build();
	    return ec2;
    }

    public static void main(String[] args)
    {
    	// TODO: setCreds(BasicAWSCredentials newCreds)
    	awsCreds = new BasicAWSCredentials("<<CHANGE TO YOURS>>", "<<CHANGE TO YOURS>>");
    	
    	//descInstances();
    	
    	String world = "UpdateAquatic";
    	String instID = getInstanceIDByMCWorldTag(world);
    	
//    	startInstance(instID);
    	System.out.println("Hello world!");
    	
    	
/*
 *     for (Map.Entry<String, String> tagFilter : tags.entrySet()) {
        // for a given tag key, OR relationship for multiple different values
        describeInstancesRequest.withFilters(
            new Filter("tag:" + tagFilter.getKey()).withValues(tagFilter.getValue())
        );
    }
    	
 */
    	
/*        final String USAGE =
            "To run this example, supply an instance id and start or stop\n" +
            "Ex: StartStopInstance <instance-id> <start|stop>\n";

        if (args.length != 1) {
            System.out.println(USAGE);
            System.exit(1);
        }

        String instance_id = args[0];

        boolean start;

        if(args[1].equals("start")) {
            start = true;
        } else {
            start = false;
        }

        if(start) {
            startInstance(instance_id);
        } else {
            stopInstance(instance_id);
        }
*/
    }
}
