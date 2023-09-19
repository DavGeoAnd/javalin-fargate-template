# Javalin Web Service on AWS Fargate

## Create IAM User Group (if needed)
1. User group name: Microservices
2. Attach permissions policies - Optional: AdministratorAccess

## Create IAM User (if needed)
1. User name: DavGeoAnd
2. Provide user access to the AWS Management Console - optional: Check
    * User type: I want to create an IAM user
3. Console password: Autogenerated password
4. Users must create a new password at next sign-in - Recommended: Uncheck
5. Permissions options: Add user to group
    * User groups: Microservices
6. Console sign-in: https://dave-aws-dga.signin.aws.amazon.com/console
7. Sign in with IAM user

## Create Load Balancer Security Group (if needed)
1. Basic details
   * Security group name: dga-ms-ecs-lb-sg
   * Description: dga-ms-ecs-lb-sg
   * VPC: dga-vpc
2. Inbound rules
   * Type: HTTP
   * Source: Anywhere-IPv4

## Create Javalin Microservice Security Group (if needed)
1. Basic details
   * Security group name: dga-ms-ecs-sg
   * Description: dga-ms-ecs-sg
   * VPC: dga-vpc
2. Inbound rules
   * Type: Custom TCP --- Port range: 10000 --- Source: Anywhere-IPv4
   * Type: HTTP --- Source: Anywhere

## Create Default Target Group (if needed)
1. Basic configuration
   * Choose a target type: IP addresses
   * Target group name: default-microservices-tg
   * Protocol: HTTP
   * Port: 80
   * IP address type: IPv4
   * VPC: dga-vpc
   * Protocol version: HTTP1
   * Health check protocol: HTTP
   * Health check path: /
2. IP addresses
   * Network: dga-vpc
   * Remove IP Address

## Create Load Balancer (if needed)
1. Load balancer types: Application Load Balancer
2. Basic configuration
   * Load balancer name: microservices
   * Scheme: Internet-facing
   * IP address type: IPv4
3. Network mapping
   * VPC: dga-vpc
   * Mappings: 1a, 1b
4. Security groups: dga-ms-ecs-lb-sg
5. Listeners and routing
    * Protocol: HTTP
    * Port: 80
    * Default action: default-microservices-tg

## Create ECR Repository
1. 'Visibility settings': Private
2. 'Repository name': javalin-fargate-template

## Create Cluster (if needed)
1. Cluster configuration
   * Cluster name: microservices
2. Infrastructure: AWS Fargate (serverless)

## Create Task Definition
1. Task definition family: javalin-fargate-template
2. Infrastructure requirements
   * Launch type: AWS Fargate
   * Operating system/Architecture: Linux/X86_64
   * Task size
     * CPU: .25 vCPU
     * Memory: .5 GB
3. Container - 1
    * Name: javalin-fargate-template
    * Image URI: 396607284401.dkr.ecr.us-east-1.amazonaws.com/javalin-fargate-template:latest
    * Port mappings
      * Container port: 10000
      * Protocol: TCP
      * App protocol: HTTP

## Create Service
1. Existing cluster: microservices
2. Compute options: Launch type
3. Service name: javalin-fargate-template
4. Desired tasks: 1
5. VPC: dga-vpc
6. Subnets
    * 1a, 1b
7. Security group: dga-ms-ecs-sg 
8. Load balancer type: Application Load Balancer
9. Application Load Balancer: Use an existing load balancer
10. Load balancer: microservices
11. Listener: Use an existing listener
     * Listener: 80:HTTP
12. Target group: Create new target group
    * Target group name: javalin-fargate-template
    * Protocol: HTTP
    * Path pattern: /template/*
    * Evaluation order: 1 (or highest evaluation order + 1)
    * Health check path: /template/health
    * Health check protocol: HTTP
    * Health check grace period: 120