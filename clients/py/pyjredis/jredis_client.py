import argparse
import sys

from prompt_toolkit import prompt

from .pyjredis import JRedisClient


def main() -> int:
	parser = argparse.ArgumentParser(description='JRedis client')
	parser.add_argument('--host', type=str, default='127.0.0.1', help='host')
	parser.add_argument('--port', type=int, default=8086, help='port')
	args = parser.parse_args()

	client = JRedisClient(args.host, args.port)
	while True:
		try:
			command = prompt(f'{args.host}:{args.port}> ')
			if command == 'exit':
				break
			ret = client.process_command(command)
			print(ret.__repr__())
		except Exception as e:
			print(e, file=sys.stderr)
			return 1
	return 0
