#!/usr/bin/python

import sys
from typing import Any, Dict

import semver
import requests

repo: str = 'jenkins-std-lib'
owner: str = 'DontShaveTheYak'

def do_action(action, version):
    function = getattr(version, action)

    new_version = function()

    print(f'{version} {action} to {new_version}')
    return new_version

def get_response(url) -> Dict[str, Any]:
    response = requests.get(url)
    return response.json()

def get_action(pull_request: str) -> str:
    valid_labels = ['major','minor','patch']
    response = get_response(f"https://api.github.com/repos/{owner}/{repo}/pulls/{pull_request}")

    label = [label['name'] for label in response['labels'] if label['name'] in valid_labels][0]
    return label

def set_output(name: str, value: str):
    print(f"::set-output name={name}::{value}")

latest_tag = sys.argv[1]
pull_request = sys.argv[2]
branch = sys.argv[3]

action_methods = {
    'patch': 'bump_patch',
    'minor': 'bump_minor',
    'major': 'bump_major'
}

if branch != "master":
    action_name = get_action(pull_request)
    action = action_methods[action_name]

next_version: str = ''

print(f'Latest tag is {latest_tag}')

response = get_response(f"https://api.github.com/repos/{owner}/{repo}/releases/latest")
release_tag = response['tag_name']

print(f'Latest release is {release_tag}')

if branch == 'master':
    print("This release is a final release!")
    base_tag = latest_tag.split("-")[0]
    bump_rule = "None"
    set_output('next_tag', base_tag)
    sys.exit(0)


if '-SNAPSHOT' in latest_tag:
    print('Checking if we can reuse latest tag.')

    latest_tag = latest_tag.split('-')[0]

    next_tag = semver.VersionInfo.parse(release_tag)

    next_tag = do_action(action, next_tag)

    latest_tag = semver.VersionInfo.parse(latest_tag)

    compare = semver.compare(str(latest_tag),str(next_tag))

    next_tag = f'{next_tag}-SNAPSHOT'
    latest_tag = f'{latest_tag}-SNAPSHOT'


    if compare == -1:
        print(f'Creating {next_tag} because its version is higher than latest tag: {latest_tag}')
        next_version = next_tag
    elif compare == 1:
        print(f'Reusing latest tag ({latest_tag}) because next tag ({next_tag}) is lower.')
        next_version = latest_tag
    else:
        print(f'Reusing latest tag ({latest_tag}) because its version is equal to next tag ({next_tag})')
        next_version = latest_tag
else:
    # create new snapshot tag and exit
    version = semver.VersionInfo.parse(latest_tag)
    new_tag = do_action(action, version)
    print(f'Creating new SNAPSHOT tag {new_tag}-SNAPSHOT')
    next_version = f'{new_tag}-SNAPSHOT'


set_output('next_tag', next_version)
