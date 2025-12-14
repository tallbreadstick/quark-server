from solution import Solution

sol = Solution()
result = sol.sum(2, 3)
status = result == 5

with open("out.txt", "w") as f:
    f.write("true" if status else "false")