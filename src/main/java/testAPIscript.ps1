# --- PowerShell Targeted Test Suite ---
# Goal: Test message persistence (logout/login) with 3 users, and verify 
# the negative case where a user leaves a chatroom and stops receiving messages.

# Configuration
$baseUrl = "http://localhost:50001"
$contentType = "application/json"
$users = @("User01", "User02", "User03", "UserL1", "UserL2") # 5 total users
$chatroomPersistence = "PersistenceRoom"
$chatroomLeave = "LeaveRoom"

# Persistence Target Configuration
$targetUser = "User03"
$targetPassword = "pass03"
$expectedCacheCount = 3 # Messages M1, M2, M3
$targetTokenNew = ""

$tokens = @{}
$pendingMessages = @{} 
$logFilePath = "api_test_log.txt"
$script:TestLog = @() # Global array to capture all log output

# Global status flags for final summary
$script:testA_status = $false 
$script:testB_status = $false

# --- Helper Functions ---

# Function to capture output to log file array and print to console
function Log-Output {
    param(
        [string]$Message,
        [string]$Color = "White" # Default color
    )
    # 1. Log to file capture array (remove color and output formatting)
    $script:TestLog += $Message
    # 2. Output to console for user feedback
    Write-Host $Message -ForegroundColor $Color
}

# Function to invoke API calls and output raw result
function Invoke-Api-Test {
    param(
        [Parameter(Mandatory=$true)][string]$Description,
        [Parameter(Mandatory=$true)][string]$Method,
        [Parameter(Mandatory=$true)][string]$Uri,
        [string]$Body = ""
    )
    
    Log-Output "`n>> [$Description]" "Yellow"
    
    $params = @{
        Method = $Method
        Uri = $Uri
        ContentType = $contentType
        ErrorAction = "Stop" 
    }
    if ($Body) {
        $params.Body = $Body
    }

    try {
        $result = Invoke-RestMethod @params
        Log-Output "   -> SUCCESS" "Green"
        
        # Output raw JSON response, captured as string
        $jsonOutput = $result | ConvertTo-Json -Depth 5 | Out-String
        Log-Output $jsonOutput "Green"
        
        return $result
    } catch {
        # Catch HTTP or other exceptions and output raw error body
        Log-Output "   -> FAILURE" "Red"
        if ($_.Exception.Response) {
            $errorResponse = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($errorResponse)
            $responseBody = $reader.ReadToEnd()
            Log-Output "$responseBody" "Red"
            
            # Return the response body as a string if parsing fails, or the parsed object
            try {
                return $responseBody | ConvertFrom-Json # Return parsed object/error
            } catch {
                return $responseBody # Return raw string on parsing failure
            }
        }
        Log-Output "   <- General Error: $($_.Exception.Message)" "Red"
        return $null
    }
}

# --- Test Execution ---

Log-Output "`n=======================================================" "White"
Log-Output " TARGETED TEST SUITE: PERSISTENCE & LEAVE ROOM LOGIC" "White"
Log-Output "=======================================================" "White"

# --- PRE-CLEANUP ---
# Attempt to log out User01 (to ensure clean login later)
$bodyLogin = '{ "username":"User01", "password":"pass01" }'
$res = Invoke-RestMethod -Method POST -ContentType $contentType -Uri "$baseUrl/user/login" -Body $bodyLogin -ErrorAction SilentlyContinue
if ($res -ne $null -and $res.token) {
    $bodyLogout = '{ "token":"' + $res.token + '" }'
    Invoke-RestMethod -Method POST -ContentType $contentType -Uri "$baseUrl/user/logout" -Body $bodyLogout -ErrorAction SilentlyContinue | Out-Null
}

# --- A. Setup: Register and Login (5 Users) ---

foreach ($user in $users) {
    $pass = "pass" + $user.Substring(4)
    $body = '{ "username":"' + $user + '", "password":"' + $pass + '" }'
    
    # Register (may fail if user exists, which is fine)
    Invoke-Api-Test -Description "A. Register $user" -Method POST -Uri "$baseUrl/user/register" -Body $body | Out-Null
    
    # Login
    $res = Invoke-Api-Test -Description "A. Login $user" -Method POST -Uri "$baseUrl/user/login" -Body $body
    
    if ($res -ne $null -and $res.token) {
        $tokens[$user] = $res.token
    } else {
        Log-Output "   -> ERROR: Failed to get token for $user during login." "Red"
    }
}


# =================================================================
# SECTION A: MESSAGE PERSISTENCE (LOGOUT/LOGIN) CHECK
# =================================================================

Log-Output "`n=======================================================" "White"
Log-Output " SECTION A: MESSAGE PERSISTENCE CHECK ($chatroomPersistence)" "White"
Log-Output "=======================================================" "White"

$senderTokenA = $tokens["User01"]
$senderTokenB = $tokens["User02"]
$targetToken = $tokens[$targetUser]

# A1. Create & Join Chatroom
$bodyCreate = '{ "chatroomName":"' + $chatroomPersistence + '", "token":"' + $senderTokenA + '" }'
Invoke-Api-Test -Description "A1. Create $chatroomPersistence (User01)" -Method POST -Uri "$baseUrl/chatroom/create" -Body $bodyCreate | Out-Null

$users[0..2] | ForEach-Object { 
    $user = $_
    $body = '{ "chatroomName":"' + $chatroomPersistence + '", "token":"' + $tokens[$user] + '" }'
    Invoke-Api-Test -Description "A1. Join $user to $chatroomPersistence" -Method POST -Uri "$baseUrl/chatroom/join" -Body $body | Out-Null
}

# A2. Clear Target User Queue
$bodyPollTarget = '{ "token":"' + $targetToken + '" }'
Invoke-Api-Test -Description "A2. Poll $targetUser to Clear Queue" -Method POST -Uri "$baseUrl/chat/poll" -Body $bodyPollTarget

# A3. Send Messages that MUST be cached for TargetUser (User03)
# M1: Direct Message (DM) from User01 to User03
$msg1 = "M1: DM from User01 (PERSISTENT)"
$body = '{ "username":"' + $targetUser + '", "message":"' + $msg1 + '", "token":"' + $senderTokenA + '" }'
Invoke-Api-Test -Description "A3. M1. Send DM User01 -> $targetUser" -Method POST -Uri "$baseUrl/chat/send" -Body $body | Out-Null
$pendingMessages[1] = $msg1

# M2: Chatroom Message (from User02)
$msg2 = "M2: CR Message from User02 (PERSISTENT)"
$body = '{ "chatroomName":"' + $chatroomPersistence + '", "message":"' + $msg2 + '", "token":"' + $senderTokenB + '" }'
Invoke-Api-Test -Description "A3. M2. Send CR $chatroomPersistence" -Method POST -Uri "$baseUrl/chatroom/send" -Body $body | Out-Null
$pendingMessages[2] = $msg2

# M3: Another Direct Message (DM) from User01 to User03
$msg3 = "M3: DM from User01 (PERSISTENT)"
$body = '{ "username":"' + $targetUser + '", "message":"' + $msg3 + '", "token":"' + $senderTokenA + '" }'
Invoke-Api-Test -Description "A3. M3. Send DM User01 -> $targetUser" -Method POST -Uri "$baseUrl/chat/send" -Body $body | Out-Null
$pendingMessages[3] = $msg3

# A4. Target User (User03) logs out
Log-Output "`n>> [A4. $targetUser Logging Out (Triggering Caching)]" "Yellow"
$bodyLogout = '{ "token":"' + $targetToken + '" }'
Invoke-RestMethod -Method POST -ContentType $contentType -Uri "$baseUrl/user/logout" -Body $bodyLogout -ErrorAction SilentlyContinue | Out-Null

# A5. Target User (User03) logs back in
$bodyLogin = '{ "username":"' + $targetUser + '", "password":"' + $targetPassword + '" }'
$res = Invoke-Api-Test -Description "A5. $targetUser Logging In (Retrieving Cache)" -Method POST -Uri "$baseUrl/user/login" -Body $bodyLogin
$targetTokenNew = $res.token

# A6. Target User (User03) polls with new token (Persistence Check)
$bodyPollNew = '{ "token":"' + $targetTokenNew + '" }'
$pollFinal = Invoke-Api-Test -Description "A6. Poll $targetUser (Expecting $expectedCacheCount Cached Messages)" -Method POST -Uri "$baseUrl/chat/poll" -Body $bodyPollNew

# A7. Verification
$receivedCount = ($pollFinal.messages | Measure-Object).Count
if ($receivedCount -eq $expectedCacheCount) {
    Log-Output "SUCCESS: Persistence verified! All $expectedCacheCount messages retrieved." "Green"
    $script:testA_status = $true
} else {
    Log-Output "FAILURE: Persistence check failed. Expected $expectedCacheCount, received $receivedCount." "Red"
}


# =================================================================
# SECTION B: LEAVE CHATROOM LOGIC CHECK
# =================================================================

Log-Output "`n=======================================================" "White"
Log-Output " SECTION B: LEAVE ROOM CHECK ($chatroomLeave)" "White"
Log-Output "=======================================================" "White"

$leaverUser = "UserL2"
$senderTokenL1 = $tokens["UserL1"]
$leaverTokenL2 = $tokens[$leaverUser]
$expectedLeavePollCount = 0 # Should receive 0 messages after leaving

# B1. Create & Join Chatroom
$bodyCreate = '{ "chatroomName":"' + $chatroomLeave + '", "token":"' + $senderTokenL1 + '" }'
Invoke-Api-Test -Description "B1. Create $chatroomLeave (UserL1)" -Method POST -Uri "$baseUrl/chatroom/create" -Body $bodyCreate | Out-Null

# UserL2 joins
$bodyJoin = '{ "chatroomName":"' + $chatroomLeave + '", "token":"' + $leaverTokenL2 + '" }'
Invoke-Api-Test -Description "B1. $leaverUser joins $chatroomLeave" -Method POST -Uri "$baseUrl/chatroom/join" -Body $bodyJoin | Out-Null

# B2. Test Message 1 (Before leaving)
$msgTest1 = "Test1: BEFORE LEAVE"
$bodySend = '{ "chatroomName":"' + $chatroomLeave + '", "message":"' + $msgTest1 + '", "token":"' + $senderTokenL1 + '" }'
Invoke-Api-Test -Description "B2. UserL1 sends Msg 1 (Should be received)" -Method POST -Uri "$baseUrl/chatroom/send" -Body $bodySend | Out-Null

# B3. Poll and clear message queue for UserL2
$bodyPollL2 = '{ "token":"' + $leaverTokenL2 + '" }'
$pollBeforeLeave = Invoke-Api-Test -Description "B3. $leaverUser Polls (Expects 1 message)" -Method POST -Uri "$baseUrl/chat/poll" -Body $bodyPollL2

# B4. UserL2 leaves the chatroom
$bodyLeave = '{ "chatroomName":"' + $chatroomLeave + '", "token":"' + $leaverTokenL2 + '" }'
Invoke-Api-Test -Description "B4. $leaverUser leaves $chatroomLeave" -Method POST -Uri "$baseUrl/chatroom/leave" -Body $bodyLeave | Out-Null

# B5. Test Message 2 (After leaving)
$msgTest2 = "Test2: AFTER LEAVE (SHOULD NOT BE RECEIVED)"
$bodySend = '{ "chatroomName":"' + $chatroomLeave + '", "message":"' + $msgTest2 + '", "token":"' + $senderTokenL1 + '" }'
Invoke-Api-Test -Description "B5. UserL1 sends Msg 2" -Method POST -Uri "$baseUrl/chatroom/send" -Body $bodySend | Out-Null

# B6. Poll UserL2 again (Should receive 0 new messages)
$pollAfterLeave = Invoke-Api-Test -Description "B6. $leaverUser Polls (Expects $expectedLeavePollCount messages)" -Method POST -Uri "$baseUrl/chat/poll" -Body $bodyPollL2

# B7. Verification
$receivedCountLeave = ($pollAfterLeave.messages | Measure-Object).Count
if ($receivedCountLeave -eq $expectedLeavePollCount) {
    Log-Output "SUCCESS: Leave Room verified! $leaverUser received 0 messages after leaving." "Green"
    $script:testB_status = $true
} else {
    Log-Output "FAILURE: Leave Room failed. $leaverUser received $receivedCountLeave messages (Expected 0)." "Red"
}


# --- C. Summary and Final Cleanup ---

# C1. Final Summary
Log-Output "`n=======================================" "White"
Log-Output " OVERALL TEST SUMMARY" "White"
Log-Output "=======================================" "White"

$finalStatus = "PASS"
$color = "Green"

if (-not $script:testA_status -or -not $script:testB_status) {
    $finalStatus = "FAIL"
    $color = "Red"
}

# Fix for older PowerShell versions (No ternary operator support)
$statusA = "FAIL"; $colorA = "Red"
if ($script:testA_status) { $statusA = "PASS"; $colorA = "Green" }

$statusB = "FAIL"; $colorB = "Red"
if ($script:testB_status) { $statusB = "PASS"; $colorB = "Green" }

Log-Output "SECTION A (Message Persistence): $statusA" $colorA
Log-Output "SECTION B (Leave Room Check): $statusB" $colorB
Log-Output "OVERALL RESULT: $finalStatus" $color

# C2. Write captured log data to file
$script:TestLog | Out-File $logFilePath -Encoding UTF8 -Force
Log-Output "`n--- TEST LOG SAVED ---" "White"
Log-Output "Full execution log saved to: $logFilePath" "White"

# C3. Cleanup
# Logout target user's new session
$bodyLogoutNew = '{ "token":"' + $targetTokenNew + '" }'
Invoke-RestMethod -Method POST -ContentType $contentType -Uri "$baseUrl/user/logout" -Body $bodyLogoutNew -ErrorAction SilentlyContinue | Out-Null

# Logout all other users
$users | ForEach-Object { 
    $user = $_
    $originalToken = $tokens[$user]
    
    # Use the target user's new token if it's User03
    if ($user -eq $targetUser) { $tokenToLogout = $targetTokenNew }
    # Use the original token if not the target user
    elseif ($tokens.ContainsKey($user)) { $tokenToLogout = $tokens[$user] }
    else { return } # Skip if no token was ever assigned

    $bodyLogout = '{ "token":"' + $tokenToLogout + '" }'
    Invoke-RestMethod -Method POST -ContentType $contentType -Uri "$baseUrl/user/logout" -Body $bodyLogout -ErrorAction SilentlyContinue | Out-Null
}