The following instructions are to run the docker image for the NoSQL Database cloud service on an
IaaS instance in the Oracle OCI.

1.  Spin up a VM Standard 2.4 (4 cores with 28 GB memory)
2.  You will need to open up port 8443 in the VCN where you have launched your instance.
    a).  Go to the VCN managment page (e.g. for Ashburn - https://console.us-ashburn-1.oraclecloud.com/a/networking/vcns)
    b).  Go to the security lists for this VCN
    c).  Add the following stateful ingress rule opening up port 8443
        Source: 0.0.0.0/0
        IP Protocol: TCP
        Source Port Range: All
        Destination Port Range: 8443
        Allows: TCP traffic for ports: 8443

3.  ssh into your instance and execute the following shell commands to enable the firewall to allow traffic on port 8443
    sudo firewall-cmd --zone=public --permanent --add-port=8443/tcp
    sudo firewall-cmd --reload

4.  Install Docker
        sudo yum install -y yum-utils device-mapper-persistent-data lvm2
        sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
        sudo yum-config-manager --enable docker-ce-edge
        sudo yum-config-manager --enable docker-ce-test
        sudo yum install docker-ce
        sudo systemctl start docker
        sudo docker run hello-world

5. To run the demo, issue the fololowing command from the shell of your VM.  NOTE that the command below will
    download the docker image from DockerHub and then execute it.

    sudo docker run --rm --env-file creds --network host --name demo drubin99/ndcs_demo_v1


    You must supply a file containing your credentials, above we have called it creds.  This file contains:

        region_uri=The NoSQL Cloud Service URI for the region you will connect to.  (e.g. ndcs.eucom-central-1.oraclecloud.com)
        idcs_url=your IDCS URL goes here (e.g. https://idcs-270644901bf04406ae7180bd3d995ce6.identity.oraclecloud.com/)
        entitlement_id=your entitlement ID goes here
        andc_username=your cloud user name goes here
        andc_user_pwd=the password for your cloud user goes here
        andc_client_id=The application client ID from IDCS (e.g. ANDCApp-55037d8ce7f0402196daad50e90ea252_APPID)
        andc_client_secret=The application client secret from IDCS
        skip_connection_test=true | false - If true, the credentials will be tested before the application comes up

    To shutdown the application:

        sudo docker stop demo

6.  To connect to the application:
        https://id_address_or_your_oci_istance:8443/NDCSDemo/NDCSDemo.html

        user name: ndcsDemoUser
        password: nosqlOracle!!
