//
//  ViewController.m
//  Mayo-ipad
//
//  Created by Rishabh Srivastava on 08/10/14.
//  Copyright (c) 2014 rishabh srivastava. All rights reserved.
//

#import "ViewController.h"

#import "XMLRPCRequest.h"
#import "XMLRPCConnection.h"


@interface ViewController ()

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view, typically from a nib.
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}




- (IBAction)login:(id)sender {
    
    
    
    NSDictionary *retLogin = [self loginToServerWithUser:self.userName.text passwd:self.passWord.text];
    NSLog((retLogin != nil) ? @"True" : @"False");
   

    if(retLogin != nil) {
        
        
        [self performSegueWithIdentifier:@"login_success" sender:self];
    }
    
    else{
        
        
        
        [[[UIAlertView alloc] initWithTitle:@"Sorry"
                                    message:@"Username password cannot be authenticated, TRY AGAIN!"
                                   delegate: nil
                          cancelButtonTitle:@"Cancel"
                          otherButtonTitles:nil
          ] show];
    }
    
    //login works
    
    //mimicing image upload
    
    /*
    
    
    NSDictionary *retUpload = [self uploadImageToServerWithUser:@"rishabh" passwd:@"sundevil"];
    NSLog((retUpload != nil) ? @"True" : @"False");

    */
    
/*
    
    NSInteger success = 0;
    @try {
        
        if([[self.userName text] isEqualToString:@"rishabh"] || [[self.passWord text] isEqualToString:@"sundevil"] ) {
            
            [self alertStatus:@"Please enter Username and Password" :@"Sign in Failed!" :0];
            
        } else {
            NSString *post =[[NSString alloc] initWithFormat:@"username=%@&password=%@",[self.userName text],[self.passWord text]];
            NSLog(@"PostData: %@",post);
            
            NSURL *url=[NSURL URLWithString:@"http://raostravelogue.com/roller-services/xmlrpc"];
            NSLog(@"There");
            
            NSData *postData = [post dataUsingEncoding:NSASCIIStringEncoding allowLossyConversion:YES];
            
            NSString *postLength = [NSString stringWithFormat:@"%lu", (unsigned long)[postData length]];
            
            NSMutableURLRequest *request = [[NSMutableURLRequest alloc] init];
            [request setURL:url];
            [request setHTTPMethod:@"POST"];
            [request setValue:postLength forHTTPHeaderField:@"Content-Length"];
            [request setValue:@"application/json" forHTTPHeaderField:@"Accept"];
            [request setValue:@"application/x-www-form-urlencoded" forHTTPHeaderField:@"Content-Type"];
            [request setHTTPBody:postData];
            
            //[NSURLRequest setAllowsAnyHTTPSCertificate:YES forHost:[url host]];
            
            NSError *error = [[NSError alloc] init];
            NSHTTPURLResponse *response = nil;
            NSData *urlData=[NSURLConnection sendSynchronousRequest:request returningResponse:&response error:&error];
            
            NSLog(@"Response code: %ld", (long)[response statusCode]);
            
            if ([response statusCode] >= 200 && [response statusCode] < 300)
            {
                NSString *responseData = [[NSString alloc]initWithData:urlData encoding:NSUTF8StringEncoding];
                NSLog(@"Response ==> %@", responseData);
                
                NSError *error = nil;
                NSDictionary *jsonData = [NSJSONSerialization
                                          JSONObjectWithData:urlData
                                          options:NSJSONReadingMutableContainers
                                          error:&error];
                
                success = [jsonData[@"success"] integerValue];
                NSLog(@"Success: %ld",(long)success);
                
                if(success == 1)
                {
                    NSLog(@"Login SUCCESS");
                } else {
                    
                    NSString *error_msg = (NSString *) jsonData[@"error_message"];
                    [self alertStatus:error_msg :@"Sign in Failed!" :0];
                }
                
            } else {
                //if (error) NSLog(@"Error: %@", error);
                [self alertStatus:@"Connection Failed" :@"Sign in Failed!" :0];
            }
        }
    }
    @catch (NSException * e) {
        NSLog(@"Exception: %@", e);
        [self alertStatus:@"Sign in Failed." :@"Error!" :0];
    }
    if (success) {
        [self performSegueWithIdentifier:@"login_success" sender:self];
    }
 */
}

- (void) alertStatus:(NSString *)msg :(NSString *)title :(int) tag
{
    UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:title
                                                        message:msg
                                                       delegate:self
                                              cancelButtonTitle:@"Ok"
                                              otherButtonTitles:nil, nil];
    alertView.tag = tag;
    [alertView show];
    
}

- (IBAction)backgroundTap:(id)sender {
    [self.view endEditing:YES];
}

-(BOOL) textFieldShouldReturn:(UITextField *)textField {
    [textField resignFirstResponder];
    return YES;
    
}

- (NSDictionary *)loginToServerWithUser:(NSString *)user
                                 passwd:(NSString *)password{
    //NSString *encPassWd = [self sha1:password];
    
    NSArray *args = [[NSArray alloc]initWithObjects:@"test",user,password,nil];
    NSString *server = xmlrpcServerURL;         // the server
    NSString *method = xmlrpcLoginMethodName;                        // the method
    XMLRPCRequest *request = [[XMLRPCRequest alloc] initWithHost:[NSURL URLWithString:server]];
    [request setMethod:method withObjects:args];
    id response = [self executeXMLRPCRequest:request];
    
    
    if( [response isKindOfClass:[NSError class]] ) {
        return nil;
    }
    else {
        return response;          // the response key
    }
}

- (NSDictionary *)uploadImageToServerWithUser:(NSString *)user
                                       passwd:(NSString *)password{
    NSMutableDictionary *inputMap = [[NSMutableDictionary alloc] init];
    
    UIImage *uploadImage = [UIImage imageNamed:@"IMG_4756.JPG"];
    NSData *dataBits = UIImageJPEGRepresentation(uploadImage, 1);
    
    
    [inputMap setObject:@"photo.JPG" forKey:@"name"];
    [inputMap setObject:@"image/jpeg" forKey:@"type"];
    [inputMap setObject:dataBits forKey:@"bits"];
    
    
    NSArray *args = [[NSArray alloc]initWithObjects:xmlrpcBlogIDName,user,password,inputMap,nil];
    NSString *server = xmlrpcServerURL;         // the server URL
    NSString *method = xmlrpcUploadMethodName;                        // the method
    XMLRPCRequest *request = [[XMLRPCRequest alloc] initWithHost:[NSURL URLWithString:server]];
    [request setMethod:method withObjects:args];
    id response = [self executeXMLRPCRequest:request];
    
    
    if( [response isKindOfClass:[NSError class]] ) {
        return nil;
    }
    else {
        return response;          // the response key
    }
}


- (id)executeXMLRPCRequest:(XMLRPCRequest *)req {
    XMLRPCResponse *userInfoResponse = [XMLRPCConnection sendSynchronousXMLRPCRequest:req];
    return userInfoResponse;
}



@end
