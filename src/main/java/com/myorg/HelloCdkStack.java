package com.myorg;

import software.amazon.awscdk.core.*;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.VpcProps;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ClusterProps;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.ecs.patterns.*;

public class HelloCdkStack extends Stack {
    public HelloCdkStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public HelloCdkStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // VPC Construct
        Vpc vpc = new Vpc(this, "MyCDKVPC", VpcProps.builder().maxAzs(2).build());

        // Cluster Construct
        Cluster cluster = new Cluster(this, "Cluster", ClusterProps.builder().vpc(vpc).build());

        // Load Balanced Fargate Construct
//        NetworkLoadBalancedFargateService fargateService = new NetworkLoadBalancedFargateService(
//                this,
//                "FargateService",
//                NetworkLoadBalancedFargateServiceProps.builder()
//                        .cluster(cluster)
//                        .taskImageOptions(NetworkLoadBalancedTaskImageOptions.builder()
//                                .image(ContainerImage.fromRegistry("amazon/amazon-ecs-sample"))
//                                .containerPort(80)
//                                .build())
//                        .build());
        ApplicationLoadBalancedFargateService fargateService = ApplicationLoadBalancedFargateService.Builder.create(this, "MyFargateService")
                .cluster(cluster)           // Required
                .cpu(512)                   // Default is 256
                .desiredCount(1)            // Default is 1
                .taskImageOptions(
                        ApplicationLoadBalancedTaskImageOptions.builder()
                                .image(ContainerImage.fromRegistry("amazon/amazon-ecs-sample"))
                                .build())
                .memoryLimitMiB(2048)       // Default is 512
                .publicLoadBalancer(true)   // Default is false
                .build();

        // Output the DNS where you can access your service
        new CfnOutput(this, "LoadBalancerDNS",
                CfnOutputProps.builder().value(fargateService.getLoadBalancer().getLoadBalancerDnsName()).build());

    }
}
