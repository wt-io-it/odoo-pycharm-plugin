from odoo.http import Controller, request


class MyController(Controller):

    def my_method(self):
        existing_obj = request.env['e<caret>xisting']
        existing_obj.do_something()
