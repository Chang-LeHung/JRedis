class JRedisException(Exception):
	"""
	Base class for JRedis exceptions.
	"""

	def __init__(self, *args):
		super().__init__(*args)
