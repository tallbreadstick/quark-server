# Test Case 2: Different values
from solution import Solution

sol = Solution()
result = sol.twoSum([3, 2, 4], 6)
status = result == [1, 2]

with open("out.txt", "w") as f:
    f.write("true" if status else "false")