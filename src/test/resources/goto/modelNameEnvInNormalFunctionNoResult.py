import os


def my_setup_function():
    env = os.environ.copy()
    # this is not a magic odoo env attribute and existing should not be resolved
    env['e<caret>xisting'] = {}