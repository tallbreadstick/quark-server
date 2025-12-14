from typing import List

class Solution:
    def twoSum(self, nums: List[int], target: int) -> List[int]:
        n = len(nums)
        map = {}
        for i in range(n):
            if target - nums[i] in map:
                return [map[target - nums[i]], i]
            map[nums[i]] = i