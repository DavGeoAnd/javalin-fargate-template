# Javalin Web Service on AWS Fargate

## Create IAM User Group (if needed)
1. 'User group name': Microservices
2. 'Attach permissions policies - Optional': AdministratorAccess

## Create IAM User (if needed)
1. 'User name': DavGeoAnd
2. Check 'Provide user access to the AWS Management Console - optional'
   * Choose 'I want to create an IAM user'
3. Choose 'Autogenerated password'
4. Uncheck 'Users must create a new password at next sign-in - Recommended'
5. Choose 'Add user to group'
    * 'Group Name': Microservices
6. Console sign-in: https://dave-aws-dga.signin.aws.amazon.com/console
7. Sign in with IAM user

## Create Load Balancer (if needed)
1. 'Load balancer types': Application Load Balancer
2. 'Load balancer name': dga-ms-ecs-lb
3. 'Scheme': Internet-facing
4. 'IP address type': IPv4
5. 'VPC': dga-vpc
6. 'Mappings': 1a / 1d
7. 'Security groups':  dga-ms-ecs-lb-sg
   * 'Security group name': dga-ms-ecs-lb-sg
   * 'Inbound rules':
     * 'Type': HTTP
     * 'Source': Anywhere-IPv4
8. Create Default Target Group
   * 'Choose a target type': Instances
   * 'Target group name': default-tg

## Create Cluster (if needed)
1. 'Cluster name': dga-ms-cluster
2. Choose: 'AWS Fargate (serverless)'

## Create Task Definition
1. 'Task definition family': javalin-fargate-template-td
2. Choose: 'AWS Fargate'
3. Task size:
   * 'CPU': .25 vCPU
   * 'Memory': .5 GB
4. 'Task execution role': ecsTaskExecutionRole
5. 'Container - 1':
   * 'Name': javalin-fargate-template
   * 'Image URI': dgandalcio/javalin-fargate-template:latest
   * 'Container port': 10000

## Create Service
1. 'Existing cluster': dga-ms-cluster
2. 'Compute options': Launch type
3. 'Service name': javalin-fargate-template-svc
4. 'Subnets': 1a / 1d
5. 'Security group': dga-ms-ecs-sg
   * 'Security group name': dga-ms-ecs-sg
   * 'Security group description': open ports for dga microservices
   * 'Inbound rules for security groups'
     * 'Type': Custom TCP --- 'Port range': 10000 --- 'Source': Anywhere
     * 'Type': HTTP --- 'Source': Anywhere
6. 'Load balancer type': Application Load Balancer
7. 'Load balancer': dga-ms-ecs-lb
8. 'Listener': Use an existing listener - 80:HTTP
9. 'Target group': Create new target group
   * 'Target group name': javalin-fargate-template-tg
   * 'Protocol': HTTP
   * 'Path pattern': /template/*
   * 'Evaluation order': 1 (or highest evaluation order + 1)
   * 'Health check path': /template/health
   * 'Health check grace period': 120

## Create CodeCommit Repository
1. 'Repository name': javalin-fargate-template

## Create ECR Repository
1. 'Visibility settings': Private
2. 'Repository name': javalin-fargate-template

## Create Code Pipeline
1. 'Pipeline name': javalin-fargate-template-pipeline
2. 'Service role': New service role
3. 'Artifact store': Default location
4. 'Source provider': GitHub (Version 2)
5. 'Connection': DavGeoAnd
6. 'Output artifact format': Full Clone
7. 'Create Project'
   * 'Project name': javalin-fargate-template-build
   * 'Environment image': Managed Image
   * 'Operating system': Ubuntu
   * 'Runtime(s)': Standard
   * 'Image': latest
   * Check 'Enable this flag if you want to build Docker images or want your builds to get elevated privileges
   * 'Build specifications': Use a buildspec file
   * 'Environment variables': GIT_BRANCH --- #{SourceVariables.BranchName}
8. 'Deploy provider': Amazon ECS
9. 'Cluster name': dga-ms-cluster
10. 'Service name': javalin-fargate-template-svc
11. 'Image definitions file - optional': imagedefinitions.json