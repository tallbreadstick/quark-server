# Code Execution System

This system allows safe execution of user-submitted code against test cases using Docker containers for sandboxing.

## Architecture

### Components

1. **TestCase Entity** - Stores test cases for coding problems
   - Contains driver code, expected output, time limits
   - Linked to Section (coding problems)

2. **CodeExecutionService** - Executes code in sandboxed Docker containers
   - Creates temporary workspace for each submission
   - Runs code with resource limits (memory, CPU, network)
   - Parses results and validates against expected output

3. **CodeExecutionController** - REST API endpoints
   - `/api/code/submit` - Submit code and get test results
   - `/api/code/run` - Run code without saving progress

## Setup

### Prerequisites

1. **Docker** must be installed and running
   ```bash
   docker pull python:3.11-slim
   ```

2. **Database Migration** - Add test_cases table:
   ```sql
   CREATE TABLE test_cases (
       id INT AUTO_INCREMENT PRIMARY KEY,
       section_id INT NOT NULL,
       idx INT NOT NULL,
       name VARCHAR(255),
       driver_code TEXT NOT NULL,
       expected_output TEXT,
       hidden BOOLEAN NOT NULL DEFAULT FALSE,
       time_limit_ms INT DEFAULT 5000,
       points INT DEFAULT 1,
       FOREIGN KEY (section_id) REFERENCES sections(id) ON DELETE CASCADE,
       INDEX idx_section_id (section_id)
   );
   ```

## Usage

### 1. Create Test Cases for a Section

Example test case data for a Two Sum problem:

```json
{
  "sectionId": 5,
  "idx": 1,
  "name": "Basic case with positive numbers",
  "driverCode": "from solution import Solution\n\ndriver = Solution()\nresult = driver.twoSum([2, 7, 11, 15], 9)\nexpected = [0, 1]\nstatus = result == expected\nprint(status)",
  "expectedOutput": "[0, 1]",
  "hidden": false,
  "timeLimitMs": 5000,
  "points": 1
}
```

### 2. Submit Code for Testing

**Endpoint:** `POST /api/code/submit`

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

**Request Body:**
```json
{
  "activityId": 1,
  "sectionId": 5,
  "language": "python",
  "code": "class Solution:\n    def twoSum(self, nums: List[int], target: int) -> List[int]:\n        seen = {}\n        for i, num in enumerate(nums):\n            complement = target - num\n            if complement in seen:\n                return [seen[complement], i]\n            seen[num] = i\n        return []"
}
```

**Response:**
```json
{
  "success": true,
  "totalTests": 3,
  "passedTests": 3,
  "failedTests": 0,
  "executionTimeMs": 245,
  "testResults": [
    {
      "testNumber": 1,
      "testName": "Basic case with positive numbers",
      "passed": true,
      "expectedOutput": "[0, 1]",
      "actualOutput": "True",
      "errorMessage": null,
      "executionTimeMs": 82
    },
    {
      "testNumber": 2,
      "testName": "No solution case",
      "passed": true,
      "expectedOutput": "[]",
      "actualOutput": "True",
      "executionTimeMs": 78
    },
    {
      "testNumber": 3,
      "testName": "Large array",
      "passed": true,
      "expectedOutput": "[500, 999]",
      "actualOutput": "True",
      "executionTimeMs": 85
    }
  ]
}
```

### 3. Writing Test Driver Code

The driver code should:
1. Import from `solution` module (the user's code)
2. Create test inputs
3. Call the solution method
4. Compare result with expected output
5. Print `True` (pass) or `False` (fail)

**Example Driver Code:**

```python
from solution import Solution

# Create solution instance
driver = Solution()

# Test case 1: Basic case
result = driver.twoSum([2, 7, 11, 15], 9)
expected = [0, 1]
status = result == expected

# Print result (True = pass, False = fail)
print(status)
```

**More Complex Example:**

```python
from solution import Solution

driver = Solution()

# Test multiple cases
test_cases = [
    ([2, 7, 11, 15], 9, [0, 1]),
    ([3, 2, 4], 6, [1, 2]),
    ([3, 3], 6, [0, 1])
]

all_passed = True
for nums, target, expected in test_cases:
    result = driver.twoSum(nums, target)
    if result != expected:
        all_passed = False
        break

print(all_passed)
```

## Security Features

1. **Docker Sandboxing** - Code runs in isolated containers
2. **Network Disabled** - `--network none` prevents external access
3. **Resource Limits**:
   - Memory: 256MB
   - CPU: 1 core
   - Time: 5 seconds (configurable per test)
4. **Temporary Files** - Cleaned up after execution
5. **No Persistent Storage** - Container removed after execution

## Error Handling

### Timeout
```json
{
  "passed": false,
  "errorMessage": "Timeout: Test exceeded time limit of 5000ms"
}
```

### Runtime Error
```json
{
  "passed": false,
  "errorMessage": "Execution error: division by zero"
}
```

### Compilation Error
```json
{
  "success": false,
  "error": "Python execution error: SyntaxError: invalid syntax"
}
```

## Future Enhancements

1. **Multiple Languages** - Add JavaScript, Java, C++, etc.
2. **Custom Test Frameworks** - Support unittest, pytest, etc.
3. **Code Analysis** - Static analysis, linting, complexity metrics
4. **Plagiarism Detection** - Compare submissions
5. **Submission History** - Track user attempts and progress
6. **Real-time Execution** - WebSocket for live feedback
7. **Code Hints** - AI-powered suggestions for failures
8. **Performance Testing** - Time/space complexity analysis

## Troubleshooting

### Docker Not Running
```
Error: Cannot connect to the Docker daemon
Solution: Start Docker Desktop or Docker service
```

### Permission Denied
```
Solution: Add user to docker group or run with appropriate permissions
```

### Image Not Found
```
Solution: docker pull python:3.11-slim
```
