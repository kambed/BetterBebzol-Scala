package model.command.product

import model.command.abstracts.BaseCommand

case class DeleteProductByIdCommand(
                                      productId: Long,
                                      userId: Long
                                   ) extends BaseCommand
